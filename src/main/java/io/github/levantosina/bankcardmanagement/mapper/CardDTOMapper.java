package io.github.levantosina.bankcardmanagement.mapper;

import io.github.levantosina.bankcardmanagement.dto.CardDTO;
import io.github.levantosina.bankcardmanagement.model.CardEntity;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class CardDTOMapper implements Function<CardEntity, CardDTO> {
    @Override
    public CardDTO apply(CardEntity cardEntity) {
        return new CardDTO(
                cardEntity.getCardHolderName(),
                cardEntity.getEncryptedCardNumber(),
                cardEntity.getExpiryDate(),
                cardEntity.getBalance(),
                cardEntity.getCardStatus()
        );
    }
}
