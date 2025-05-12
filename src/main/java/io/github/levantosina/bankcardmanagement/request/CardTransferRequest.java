package io.github.levantosina.bankcardmanagement.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CardTransferRequest(
        @NotNull(message = "From Card ID is required")
        @Positive(message = "From Card ID must be a positive number")
        Long fromCardId,

        @NotNull(message = "To Card ID is required")
        @Positive(message = "To Card ID must be a positive number")
        Long toCardId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount
) {}
