package com.nexturn.ccms.dto;

import java.math.BigDecimal;

public record CreditCardApplicationRequest(
        Integer cardTypeId,
        BigDecimal requestedCreditLimit
) {
}
