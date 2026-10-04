package com.nexturn.ccms.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.nexturn.ccms.enums.CardNetwork;
import com.nexturn.ccms.enums.CreditCardApplicationStatus;

public record CreditCardApplicationResponse(
        String applicationId,
        Integer customerId,
        String customerName,
        String customerEmail,
        Integer cardTypeId,
        String cardTypeName,
        CardNetwork network,
        String productDescription,
        BigDecimal annualFee,
        BigDecimal requestedCreditLimit,
        CreditCardApplicationStatus status,
        LocalDateTime applicationDate,
        LocalDateTime reviewedDate,
        String reviewedBy,
        String rejectionReason,
        String issuedCardLastFour
) {
}
