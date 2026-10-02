package com.nexturn.ccms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.nexturn.ccms.enums.CardStatus;

public record CreditCardResponse(
        String cardNumber,
        Integer customerId,
        Integer cardTypeId,
        BigDecimal creditLimit,
        BigDecimal availableLimit,
        BigDecimal outstandingBalance,
        LocalDate issueDate,
        LocalDate expiryDate,
        CardStatus cardStatus,
        String remarks
) {
}