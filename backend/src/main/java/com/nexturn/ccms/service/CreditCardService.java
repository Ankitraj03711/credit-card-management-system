package com.nexturn.ccms.service;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.nexturn.ccms.dto.CreditCardRequest;
import com.nexturn.ccms.dto.CreditCardResponse;

public interface CreditCardService {

    CreditCardResponse issueCard(CreditCardRequest request);

    List<CreditCardResponse> getAllCards();

    CreditCardResponse getCardByCardNumber(
            String cardNumber);

    String getCardCvv(
            String cardNumber,
            Authentication authentication);

    List<CreditCardResponse> getCardsByCustomerId(
            Integer customerId);

    CreditCardResponse activateCard(String cardNumber);

    CreditCardResponse blockCard(String cardNumber);

    CreditCardResponse unblockCard(String cardNumber);

    CreditCardResponse closeCard(String cardNumber);
}