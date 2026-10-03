package com.nexturn.ccms.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.nexturn.ccms.dto.TransactionRequest;
import com.nexturn.ccms.dto.TransactionResponse;
import com.nexturn.ccms.dto.TransactionSummaryResponse;

public interface TransactionService {

    TransactionResponse createTransaction(TransactionRequest request);
    List<TransactionResponse> getTransactionsByCardNumber(String cardNumber);
    TransactionResponse getTransactionById(UUID transactionId);
    List<TransactionResponse> getTransactionsByCustomerId(Long customerId);
    List<TransactionResponse> filterTransactions(String cardNumber, String type, LocalDate fromDate, LocalDate toDate, Double minAmount, Double maxAmount);
    TransactionSummaryResponse getTransactionSummary(String cardNumber);
}