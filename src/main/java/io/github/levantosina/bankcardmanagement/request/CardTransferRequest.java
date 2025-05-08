package io.github.levantosina.bankcardmanagement.request;

import java.math.BigDecimal;

public record CardTransferRequest(
        Long fromCardId,
        Long toCardId,
        BigDecimal amount
) {}
