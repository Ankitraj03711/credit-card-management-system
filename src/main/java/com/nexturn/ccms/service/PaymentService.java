package com.nexturn.ccms.service;

import java.time.LocalDate;
import java.util.List;

import com.nexturn.ccms.dto.PaymentRequest;
import com.nexturn.ccms.dto.PaymentResponse;
import com.nexturn.ccms.dto.PaymentSummaryResponse;

public interface PaymentService {

    PaymentResponse createPayment(PaymentRequest request);
    PaymentResponse getPaymentByReference(String paymentReference);
    List<PaymentResponse> getPaymentsByCardNumber(String cardNumber);
    List<PaymentResponse> getPaymentsByCustomerId(Long customerId);
    List<PaymentResponse> filterPayments(String cardNumber, String mode, String status, LocalDate fromDate, LocalDate toDate, Double minAmount, Double maxAmount);
    PaymentSummaryResponse getPaymentSummary(String cardNumber);
}

