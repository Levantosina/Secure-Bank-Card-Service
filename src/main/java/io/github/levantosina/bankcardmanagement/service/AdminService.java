package io.github.levantosina.bankcardmanagement.service;

import io.github.levantosina.bankcardmanagement.dto.CardDTO;
import io.github.levantosina.bankcardmanagement.dto.UserAdminDTO;
import io.github.levantosina.bankcardmanagement.exception.CardStatusException;
import io.github.levantosina.bankcardmanagement.exception.RequestValidationException;
import io.github.levantosina.bankcardmanagement.exception.ResourceNotFoundException;
import io.github.levantosina.bankcardmanagement.mapper.UserAdminDTOMapper;
import io.github.levantosina.bankcardmanagement.model.CardEntity;
import io.github.levantosina.bankcardmanagement.model.CardStatus;
import io.github.levantosina.bankcardmanagement.model.UserAdminEntity;
import io.github.levantosina.bankcardmanagement.repository.CardRepository;
import io.github.levantosina.bankcardmanagement.repository.UserAdminRepository;
import io.github.levantosina.bankcardmanagement.request.AdminCardRegistrationRequest;
import io.github.levantosina.bankcardmanagement.request.UserUpdateRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AdminService {

    private final UserAdminRepository userAdminRepository;
    private final CardRepository cardRepository;
    private final UserAdminDTOMapper userDTOMapper;
    private final PasswordEncoder passwordEncoder;
    private final AESService aesService;
    private final ExtractUserIdFromContext extractUserIdFromContext;

    @Transactional
    public List<UserAdminDTO> findAllUsers() {
        return userAdminRepository.findAll()
                .stream()
                .map(userDTOMapper)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserAdminDTO findUserById(Long userId) {
        return userAdminRepository.findById(userId)
                .map(userDTOMapper)
                .orElseThrow(()-> new ResourceNotFoundException("User with id [%s] not found".
                                formatted(userId)));
    }

    @Transactional
    public UserAdminDTO updateUserAdmin(Long userId, UserUpdateRequest userUpdateRequest) {
        UserAdminEntity user = userAdminRepository.findUserAdminByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with [%s] not found".formatted(userId)));

        if (userUpdateRequest.email() != null && !userUpdateRequest.email().equals(user.getEmail())) {
            if (userAdminRepository.existsByEmail(userUpdateRequest.email())) {
                throw new IllegalArgumentException("Email address is already taken.");
            }
            user.setEmail(userUpdateRequest.email());
        }
        boolean changes = false;

        if (userUpdateRequest.email() != null && !userUpdateRequest.email().equals(user.getEmail())) {
            user.setEmail(userUpdateRequest.email());
            changes = true;
        }
        if (userUpdateRequest.password() != null && !userUpdateRequest.password().equals(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(userUpdateRequest.password()));
            changes = true;
        }
        if (!changes) {
            throw new RequestValidationException("No changes for id with [%s] detected".formatted(userId));
        }

        UserAdminEntity updated = userAdminRepository.save(user);
        return userDTOMapper.apply(updated);
    }


    @Transactional
    public void deleteUser(Long userId) {
        if(!userAdminRepository.existsUserAdminByUserId(userId)){
            throw new ResourceNotFoundException("User with id [%s] not found".formatted(userId));
        }
        userAdminRepository.deleteUserAdminByUserId(userId);
    }

    @Transactional
    public void createCard(AdminCardRegistrationRequest adminCardRegistrationRequest) throws AccessDeniedException {
        Long authenticatedUserId = extractUserIdFromContext.extractUserIdFromContext();

        UserAdminEntity userAdminEntity = userAdminRepository.findById(authenticatedUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id [%s]"
                            .formatted(authenticatedUserId)));

        String encryptedCardNumber = aesService.encrypt(adminCardRegistrationRequest.encryptedCardNumber());

        if (cardRepository.existsByEncryptedCardNumber(encryptedCardNumber)) {
            throw new IllegalArgumentException("Card with this number already exists");
        }

        CardStatus cardStatus = adminCardRegistrationRequest.expiryDate().isBefore(YearMonth.now())
                ? CardStatus.EXPIRED
                : CardStatus.ACTIVE;
        BigDecimal balance = adminCardRegistrationRequest.balance() != null
                ? adminCardRegistrationRequest.balance()
                : BigDecimal.ZERO;
        CardEntity card = CardEntity.builder()
                .cardHolderName(adminCardRegistrationRequest.cardHolderName())
                .encryptedCardNumber(encryptedCardNumber)
                .expiryDate(adminCardRegistrationRequest.expiryDate())
                .balance(balance)
                .cardStatus(cardStatus)
                .user(userAdminEntity)
                .build();
        cardRepository.save(card);
    }
    @Transactional
    public List<CardDTO> findAllCards() {
        return cardRepository.findAll()
                .stream()
                .map(card -> {
                    String decryptedCardNumber = aesService.decrypt(card.getEncryptedCardNumber());
                    String maskedCardNumber = aesService.maskCardNumber(decryptedCardNumber);
                    return new CardDTO(
                            card.getCardHolderName(),
                            maskedCardNumber,
                            card.getExpiryDate(),
                            card.getBalance(),
                            card.getCardStatus()

                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public CardDTO findCardById(Long cardId) {
        CardEntity card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found."));

        String decryptedCardNumber = aesService.decrypt(card.getEncryptedCardNumber());
        String maskedCardNumber = aesService.maskCardNumber(decryptedCardNumber);

        return new CardDTO(
                card.getCardHolderName(),
                maskedCardNumber,
                card.getExpiryDate(),
                card.getBalance(),
                card.getCardStatus()

        );
    }
    @Transactional
    public List<CardEntity> getPendingBlockRequests() {
        return cardRepository.findByBlockRequestedTrueAndCardStatusNot(CardStatus.BLOCKED);
    }
    @Transactional
    public CardEntity blockCard(Long cardId) {

        CardEntity card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card with id [%s] not found".formatted(cardId)));

        if(card.getCardStatus()==CardStatus.BLOCKED){
            throw new CardStatusException("Card with id [%s] already blocked" .formatted(cardId));
        }
        card.setCardStatus(CardStatus.BLOCKED);

        return cardRepository.save(card);
    }

    @Transactional
    public void approveBlockRequest(Long cardId) {

        CardEntity card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card with id [%s] not found".formatted(cardId)));

        if(!card.isBlockRequest()){
            throw new CardStatusException("Card with id [%s] already blocked" .formatted(cardId));
        }
        card.setCardStatus(CardStatus.BLOCKED);
        card.setBlockRequest(false);
        cardRepository.save(card);
    }

    @Transactional
    public CardEntity activateCard(Long cardId, YearMonth newExpiryDate) {

        CardEntity card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (card.getExpiryDate().isBefore(YearMonth.now())) {
            card.setExpiryDate(newExpiryDate);

            if (newExpiryDate.isAfter(YearMonth.now())) {
                card.setCardStatus(CardStatus.ACTIVE);
                return cardRepository.save(card);
            } else {
                card.setCardStatus(CardStatus.EXPIRED);
                throw new CardStatusException("New expiry date is still in the past");
            }
        }
        if (card.getCardStatus() == CardStatus.BLOCKED) {
            card.setCardStatus(CardStatus.ACTIVE);
        } else if (card.getCardStatus() == CardStatus.ACTIVE) {
            throw new CardStatusException("Card with id [%s] is already active".formatted(cardId));
        } else {
            throw new RuntimeException("Unexpected card status");
        }

        return cardRepository.save(card);
    }
    @Transactional
    public void deleteCard(Long cardId) {
        if(!cardRepository.existsCardByCardId(cardId)){
                throw new ResourceNotFoundException("Card with id [%s] not found".formatted(cardId));
        }
        cardRepository.deleteCardByCardId(cardId);
    }
}
