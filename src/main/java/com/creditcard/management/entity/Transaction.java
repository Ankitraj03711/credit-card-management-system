package com.ccms.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.ccms.enums.TransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "transaction")
public class Transaction {

    @Id
    @GeneratedValue
    private UUID transactionId;

    @ManyToOne
    @JoinColumn(name = "cardNumber", nullable = false, updatable = false)
    private CreditCardDetails card;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TransactionType transactionType;

    @Column(nullable = false, updatable = false)
    private Double amount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime transactionDate;

    @Column(length = 255, updatable = false)
    private String description;

    @Column(length = 100, updatable = false)
    private String merchant;

    protected Transaction() {
    	
    }

    public Transaction(CreditCardDetails card, TransactionType transactionType, Double amount, 
    		LocalDateTime transactionDate, String description, String merchant) {
        this.card = card;
        this.transactionType = transactionType;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.description = description;
        this.merchant = merchant;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public CreditCardDetails getCard() {
        return card;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public Double getAmount() {
        return amount;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public String getDescription() {
        return description;
    }

    public String getMerchant() {
        return merchant;
    }
}