package com.nexturn.ccms.dto;

import java.math.BigDecimal;

public record CreditCardRequest(
        Integer customerId,
        Integer cardTypeId,
        BigDecimal creditLimit
) {
}