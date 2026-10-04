package com.nexturn.ccms.entity;

import java.time.LocalDate;

import com.nexturn.ccms.enums.CardStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "creditCardDetails")
public class CreditCardDetails {

    @Id
    @Column(length = 16, updatable=false)
    private String cardNumber;

    @ManyToOne
    @JoinColumn(name = "customerId", nullable = false, updatable=false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "cardTypeId", nullable = false)
    private CardType cardType;

    @Column(nullable = false, length = 3, updatable=false)
    private String cvv;

    @Column(nullable = false, updatable=false)
    private Double creditLimit;

    @Column(nullable = false)
    private Double availableLimit;

    @Column(nullable = false)
    private Double outstandingBalance;

    @Column(nullable = false, updatable=false)
    private LocalDate issueDate;

    @Column(nullable = false, updatable=false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus cardStatus;

    @Column(length = 255)
    private String remarks;

    public CreditCardDetails() {
    }

    public CreditCardDetails(String cardNumber, Customer customer, CardType cardType, String cvv, Double creditLimit,
                             Double availableLimit, Double outstandingBalance, LocalDate issueDate, LocalDate expiryDate,
                             CardStatus cardStatus, String remarks) {
        this.cardNumber = cardNumber;
        this.customer = customer;
        this.cardType = cardType;
        this.cvv = cvv;
        this.creditLimit = creditLimit;
        this.availableLimit = availableLimit;
        this.outstandingBalance = outstandingBalance;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.cardStatus = cardStatus;
        this.remarks = remarks;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public CardType getCardType() {
        return cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public Double getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(Double creditLimit) {
        this.creditLimit = creditLimit;
    }

    public Double getAvailableLimit() {
        return availableLimit;
    }

    public void setAvailableLimit(Double availableLimit) {
        this.availableLimit = availableLimit;
    }

    public Double getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(Double outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public CardStatus getCardStatus() {
        return cardStatus;
    }

    public void setCardStatus(CardStatus cardStatus) {
        this.cardStatus = cardStatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}