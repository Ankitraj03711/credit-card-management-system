package com.nexturn.ccms.dto;


import java.time.LocalDateTime;

public class PaymentSummaryResponse {

    private long totalPayments;
    private Double totalPaidAmount;
    private Double lastPaymentAmount;
    private LocalDateTime lastPaymentDate;

    public PaymentSummaryResponse() {
    }

    public PaymentSummaryResponse(
            long totalPayments,
            Double totalPaidAmount,
            Double lastPaymentAmount,
            LocalDateTime lastPaymentDate) {

        this.totalPayments = totalPayments;
        this.totalPaidAmount = totalPaidAmount;
        this.lastPaymentAmount = lastPaymentAmount;
        this.lastPaymentDate = lastPaymentDate;
    }

    public long getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(long totalPayments) {
        this.totalPayments = totalPayments;
    }

    public Double getTotalPaidAmount() {
        return totalPaidAmount;
    }

    public void setTotalPaidAmount(Double totalPaidAmount) {
        this.totalPaidAmount = totalPaidAmount;
    }

    public Double getLastPaymentAmount() {
        return lastPaymentAmount;
    }

    public void setLastPaymentAmount(Double lastPaymentAmount) {
        this.lastPaymentAmount = lastPaymentAmount;
    }

    public LocalDateTime getLastPaymentDate() {
        return lastPaymentDate;
    }

    public void setLastPaymentDate(LocalDateTime lastPaymentDate) {
        this.lastPaymentDate = lastPaymentDate;
    }
}