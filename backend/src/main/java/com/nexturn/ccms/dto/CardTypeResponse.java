package com.nexturn.ccms.dto;

import com.nexturn.ccms.enums.CardNetwork;
import java.math.BigDecimal;

public record CardTypeResponse(
        Integer cardTypeId,
        String name,
        String description,
        BigDecimal annualFee,
        CardNetwork network
) {
}
