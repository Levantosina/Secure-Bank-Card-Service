package io.github.levantosina.bankcardmanagement.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.YearMonth;

public record AdminCardRegistrationRequest (
        @NotBlank(message = "Card holder name must not be blank")
        @Size(max = 100, message = "Card holder name must not exceed 100 characters")
        String cardHolderName,

        @NotBlank(message = " Card number must not be blank")
        String encryptedCardNumber,

        @NotNull(message = "Expiry date is required")
        YearMonth expiryDate,

        @NotNull(message = "Balance is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Balance must be zero or positive")
        BigDecimal balance,
        @NotNull(message = "User ID is required")
        @Positive(message = "User ID must be a positive number")
        Long userId
) {

}