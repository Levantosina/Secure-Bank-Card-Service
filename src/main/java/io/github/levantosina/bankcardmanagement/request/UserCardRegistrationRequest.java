package io.github.levantosina.bankcardmanagement.request;

import java.math.BigDecimal;
import java.time.YearMonth;

public record UserCardRegistrationRequest(
        String cardHolderName,
        String encryptedCardNumber,
        YearMonth expiryDate,
        BigDecimal balance
) {

}
