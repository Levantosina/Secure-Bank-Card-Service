package io.github.levantosina.bankcardmanagement.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.YearMonth;

public record UserCardRegistrationRequest(
        @NotBlank(message = "Card holder name must not be blank")
        @Size(max = 100, message = "Card holder name must not exceed 100 characters")
        String cardHolderName,

        @NotBlank(message = "Encrypted card number must not be blank")
       String encryptedCardNumber,

        @NotNull(message = "Expiry date is required")
        YearMonth expiryDate,

        @NotNull(message = "Balance is required")
        @DecimalMin(value = "0.0",message = "Balance must be zero or positive")
        BigDecimal balance
) {

}
