package io.github.levantosina.bankcardmanagement.service;

import io.github.levantosina.bankcardmanagement.dto.CardDTO;
import io.github.levantosina.bankcardmanagement.exception.ResourceNotFoundException;
import io.github.levantosina.bankcardmanagement.model.CardEntity;
import io.github.levantosina.bankcardmanagement.model.CardStatus;
import io.github.levantosina.bankcardmanagement.model.UserAdminEntity;
import io.github.levantosina.bankcardmanagement.repository.CardRepository;
import io.github.levantosina.bankcardmanagement.repository.UserAdminRepository;
import io.github.levantosina.bankcardmanagement.request.CardRegistrationRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Service
@AllArgsConstructor
public class UserService {

    private final CardRepository cardRepository;
    private final AESService aesService;
    private final ExtractUserIdFromContext extractUserIdFromContext;
    private final UserAdminRepository userAdminRepository;

    @Transactional
    public Page<CardDTO> findAllCardsByUserId(int page,int size,String cardHolderName) throws AccessDeniedException {

        Long userId = extractUserIdFromContext.extractUserIdFromContext();

        Pageable pageable = PageRequest.of(page, size);

        Page<CardEntity> cardPage;
        if (cardHolderName != null && !cardHolderName.isEmpty()) {
            cardPage = cardRepository.findByUserUserIdAndCardHolderNameContaining(userId, cardHolderName, pageable);
        } else {
            cardPage = cardRepository.findByUserUserId(userId, pageable);
        }

        return cardPage.map(card -> {
            String decryptedCardNumber = aesService.decrypt(card.getEncryptedCardNumber());
            String maskedCardNumber = aesService.maskCardNumber(decryptedCardNumber);
            return new CardDTO(
                    card.getCardHolderName(),
                    maskedCardNumber,
                    card.getExpiryDate(),
                    card.getBalance(),
                    card.getCardStatus()
            );
        });
    }


    @Transactional
    public void createCard(CardRegistrationRequest cardRegistrationRequest) throws AccessDeniedException {

        Long authenticatedUserId = extractUserIdFromContext.extractUserIdFromContext();

        UserAdminEntity userAdminEntity;

        if (isAdmin()) {
            userAdminEntity = userAdminRepository.findById(cardRegistrationRequest.userId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id [%s]"
                            .formatted(cardRegistrationRequest.userId())));
        } else {
            userAdminEntity = userAdminRepository.findById(authenticatedUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id [%s]"
                            .formatted(authenticatedUserId)));
        }
        CardStatus cardStatus = cardRegistrationRequest.expiryDate().isBefore(YearMonth.now())
                ? CardStatus.EXPIRED
                : CardStatus.ACTIVE;
        BigDecimal balance = cardRegistrationRequest.balance() != null
                ? cardRegistrationRequest.balance()
                : BigDecimal.ZERO;
        CardEntity card = CardEntity.builder()
                .cardHolderName(cardRegistrationRequest.cardHolderName())
                .encryptedCardNumber(aesService.encrypt(cardRegistrationRequest.encryptedCardNumber()))
                .expiryDate(cardRegistrationRequest.expiryDate())
                .balance(balance)
                .cardStatus(cardStatus)
                .user(userAdminEntity)
                .build();
        cardRepository.save(card);
    }




    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }

    @Transactional
    public void requestBlockCard(Long cardId) throws AccessDeniedException {
        Long userId = extractUserIdFromContext.extractUserIdFromContext();

        CardEntity card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card with id [%s] not found".formatted(cardId)));

        if (!card.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("Access denied: not your card");
        }

        if (card.getCardStatus() == CardStatus.BLOCKED) {
            throw new IllegalStateException("Card is already blocked");
        }
        if (card.isBlockRequest()) {
            throw new IllegalStateException("Block request already submitted");
        }

        card.setBlockRequest(true);
        card.setBlockRequestedAt(LocalDateTime.now());
        cardRepository.save(card);
    }

    @Transactional
    public void transferBalance(Long fromCardId, Long toCardId, BigDecimal amount) {
        if (fromCardId.equals(toCardId)) {
            throw new IllegalArgumentException("Cannot transfer to same card");
        }

        CardEntity fromCard = cardRepository.findById(fromCardId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender card not found."));

        CardEntity toCard = cardRepository.findById(toCardId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient card not found."));

        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("You do not have enough money to transfer this card");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }

    @Transactional
    public BigDecimal getBalance(Long cardId) throws AccessDeniedException {
        Long userId = extractUserIdFromContext.extractUserIdFromContext();
        CardEntity card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card with id [%s] not found".formatted(cardId)));

        if (!card.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("Access denied: not your card");
        }

        return card.getBalance();
    }
}
