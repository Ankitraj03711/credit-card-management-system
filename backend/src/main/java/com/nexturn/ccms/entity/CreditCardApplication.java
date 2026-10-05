package com.nexturn.ccms.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import com.nexturn.ccms.enums.CreditCardApplicationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "credit_card_application")
public class CreditCardApplication {

    @Id
    @Column(length = 36, nullable = false, updatable = false)
    private String applicationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_type_id", nullable = false)
    private CardType cardType;

    @Column(name = "requested_credit_limit", nullable = false, precision = 12, scale = 2)
    private BigDecimal requestedCreditLimit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CreditCardApplicationStatus status;

    @Column(name = "application_date", nullable = false, updatable = false)
    private LocalDateTime applicationDate;

    @Column(name = "reviewed_date")
    private LocalDateTime reviewedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private UserLogin reviewedBy;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_card_number", unique = true)
    private CreditCardDetails issuedCard;

    @PrePersist
    void initializeApplication() {
        if (applicationId == null) {
            applicationId = UUID.randomUUID().toString();
        }
        if (applicationDate == null) {
            applicationDate = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        }
        if (status == null) {
            status = CreditCardApplicationStatus.PENDING;
        }
    }

    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public CardType getCardType() { return cardType; }
    public void setCardType(CardType cardType) { this.cardType = cardType; }
    public BigDecimal getRequestedCreditLimit() { return requestedCreditLimit; }
    public void setRequestedCreditLimit(BigDecimal requestedCreditLimit) { this.requestedCreditLimit = requestedCreditLimit; }
    public CreditCardApplicationStatus getStatus() { return status; }
    public void setStatus(CreditCardApplicationStatus status) { this.status = status; }
    public LocalDateTime getApplicationDate() { return applicationDate; }
    public void setApplicationDate(LocalDateTime applicationDate) { this.applicationDate = applicationDate; }
    public LocalDateTime getReviewedDate() { return reviewedDate; }
    public void setReviewedDate(LocalDateTime reviewedDate) { this.reviewedDate = reviewedDate; }
    public UserLogin getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(UserLogin reviewedBy) { this.reviewedBy = reviewedBy; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public CreditCardDetails getIssuedCard() { return issuedCard; }
    public void setIssuedCard(CreditCardDetails issuedCard) { this.issuedCard = issuedCard; }
}
