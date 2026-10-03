package com.nexturn.ccms.dto;


import com.nexturn.ccms.enums.TransactionType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class TransactionRequest {

    @NotBlank(message = "Card number is required")
    private String cardNumber;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private Double amount;

    @Size(max = 255)
    private String description;

    @Size(max = 100)
    private String merchant;

    public TransactionRequest() {
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

//	public TransactionRequest(@NotBlank(message = "Card number is required") String cardNumber,
//			@NotNull(message = "Transaction type is required") TransactionType transactionType,
//			@NotNull(message = "Amount is required") @Positive(message = "Amount must be greater than zero") Double amount,
//			@Size(max = 255) String description, @Size(max = 100) String merchant) {
//		this.cardNumber = cardNumber;
//		this.transactionType = transactionType;
//		this.amount = amount;
//		this.description = description;
//		this.merchant = merchant;
//	}
    
    
}