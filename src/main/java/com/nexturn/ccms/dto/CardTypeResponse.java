package com.nexturn.ccms.dto;

import java.math.BigDecimal;

public record CardTypeResponse(
        Integer cardTypeId,
        String name,
        String description,
        BigDecimal annualFee
) {
}
