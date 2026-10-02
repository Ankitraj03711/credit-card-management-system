package com.nexturn.ccms.dto;

import java.math.BigDecimal;

public record CardTypeRequest(
        String name,
        String description,
        BigDecimal annualFee
) {
}