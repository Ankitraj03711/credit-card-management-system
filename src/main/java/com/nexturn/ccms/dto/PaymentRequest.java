package com.nexturn.ccms.dto;


import com.nexturn.ccms.enums.PaymentMode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PaymentRequest {

    @NotBlank(message = "Card number is required")
    private String cardNumber;

    @NotNull(message = "Amount is required")
    private Double amount;

    @NotNull(message = "Payment mode is required")
    private PaymentMode paymentMode;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    public PaymentRequest() {
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(PaymentMode paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
