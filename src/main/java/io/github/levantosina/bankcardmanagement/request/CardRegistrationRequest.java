package io.github.levantosina.bankcardmanagement.request;

import java.math.BigDecimal;
import java.time.YearMonth;

public record CardRegistrationRequest(
        String cardHolderName,
        String encryptedCardNumber,
        YearMonth expiryDate,
        BigDecimal balance,
        Long userId
) {

}
