package com.nexturn.ccms.dto;


public class TransactionSummaryResponse {

    private String cardNumber;
    private Long totalTransactions;
    private Double totalTransactionAmount;
    private Long purchaseCount;
    private Double purchaseAmount;
    private Long cashWithdrawalCount;
    private Double cashWithdrawalAmount;
    private Long feeCount;
    private Double feeAmount;
    private Long refundCount;
    private Double refundAmount;
    
    public TransactionSummaryResponse() {
    	// Required for DTO construction and property-based mapping.
	}


	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public Long getTotalTransactions() {
		return totalTransactions;
	}

	public void setTotalTransactions(Long totalTransactions) {
		this.totalTransactions = totalTransactions;
	}

	public Double getTotalTransactionAmount() {
		return totalTransactionAmount;
	}

	public void setTotalTransactionAmount(Double totalTransactionAmount) {
		this.totalTransactionAmount = totalTransactionAmount;
	}

	public Long getPurchaseCount() {
		return purchaseCount;
	}

	public void setPurchaseCount(Long purchaseCount) {
		this.purchaseCount = purchaseCount;
	}

	public Double getPurchaseAmount() {
		return purchaseAmount;
	}

	public void setPurchaseAmount(Double purchaseAmount) {
		this.purchaseAmount = purchaseAmount;
	}

	public Long getCashWithdrawalCount() {
		return cashWithdrawalCount;
	}

	public void setCashWithdrawalCount(Long cashWithdrawalCount) {
		this.cashWithdrawalCount = cashWithdrawalCount;
	}

	public Double getCashWithdrawalAmount() {
		return cashWithdrawalAmount;
	}

	public void setCashWithdrawalAmount(Double cashWithdrawalAmount) {
		this.cashWithdrawalAmount = cashWithdrawalAmount;
	}

	public Long getFeeCount() {
		return feeCount;
	}

	public void setFeeCount(Long feeCount) {
		this.feeCount = feeCount;
	}

	public Double getFeeAmount() {
		return feeAmount;
	}

	public void setFeeAmount(Double feeAmount) {
		this.feeAmount = feeAmount;
	}

	public Long getRefundCount() {
		return refundCount;
	}

	public void setRefundCount(Long refundCount) {
		this.refundCount = refundCount;
	}

	public Double getRefundAmount() {
		return refundAmount;
	}

	public void setRefundAmount(Double refundAmount) {
		this.refundAmount = refundAmount;
	}
    
    
}