package io.github.levantosina.bankcardmanagement.service;

import io.github.levantosina.bankcardmanagement.dto.CardDTO;
import io.github.levantosina.bankcardmanagement.exception.ResourceNotFoundException;
import io.github.levantosina.bankcardmanagement.model.CardEntity;
import io.github.levantosina.bankcardmanagement.model.CardStatus;
import io.github.levantosina.bankcardmanagement.repository.CardRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService {

    private final CardRepository cardRepository;
    private final AESService aesService;
    private final ExtractUserIdFromContext extractUserIdFromContext;

    @Transactional
    public List<CardDTO> findAllCardsByUserId() throws AccessDeniedException {

        Long userId = extractUserIdFromContext.extractUserIdFromContext();

        return cardRepository.findAllByUserUserId(userId)
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

        card.setCardStatus(CardStatus.BLOCKED);
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
