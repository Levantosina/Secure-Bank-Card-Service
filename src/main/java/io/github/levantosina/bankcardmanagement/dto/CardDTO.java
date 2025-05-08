package io.github.levantosina.bankcardmanagement.dto;

import io.github.levantosina.bankcardmanagement.model.CardStatus;

import java.math.BigDecimal;
import java.time.YearMonth;

public record CardDTO (
        String cardHolderName,
        String encryptedCardNumber,
        YearMonth expiryDate,
        BigDecimal balance,
        CardStatus cardStatus
){
}
