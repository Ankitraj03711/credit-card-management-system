package com.nexturn.ccms.serviceimpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nexturn.ccms.dto.TransactionRequest;
import com.nexturn.ccms.dto.TransactionResponse;
import com.nexturn.ccms.dto.TransactionSummaryResponse;
import com.nexturn.ccms.entity.CreditCardDetails;
import com.nexturn.ccms.entity.Transaction;
import com.nexturn.ccms.enums.CardStatus;
import com.nexturn.ccms.exception.CreditCardNotFoundException;
import com.nexturn.ccms.exception.CustomerNotFoundException;
import com.nexturn.ccms.exception.InsufficientCreditLimitException;
import com.nexturn.ccms.exception.InvalidAmountException;
import com.nexturn.ccms.exception.InvalidCardStatusException;
import com.nexturn.ccms.exception.InvalidDateRangeException;
import com.nexturn.ccms.exception.TransactionNotFoundException;
import com.nexturn.ccms.repository.CreditCardDetailsRepository;
import com.nexturn.ccms.repository.CustomerRepository;
import com.nexturn.ccms.repository.TransactionRepository;
import com.nexturn.ccms.service.TransactionService;

import jakarta.transaction.Transactional;

@Service
public class TransactionServiceImpl implements TransactionService {
	
	private final TransactionRepository transactionRepository;
	private final CreditCardDetailsRepository creditCardDetailsRepository;
	private final CustomerRepository customerRepository;

	public TransactionServiceImpl(
	        TransactionRepository transactionRepository,
	        CreditCardDetailsRepository creditCardDetailsRepository,
	        CustomerRepository customerRepository) {

	    this.transactionRepository = transactionRepository;
	    this.creditCardDetailsRepository = creditCardDetailsRepository;
	    this.customerRepository = customerRepository;
	}
	
	@Override
	@Transactional
	public TransactionResponse createTransaction(TransactionRequest request) {

	    
	    CreditCardDetails card = creditCardDetailsRepository.findById(request.getCardNumber())
	                    .orElseThrow(() -> new CreditCardNotFoundException("credit card not available of this card number"));

	    
	    if (card.getCardStatus() != CardStatus.ACTIVE)
	        throw new InvalidCardStatusException("card is not active..Transaction cannot be performed!"); 
	    
	    if (request.getAmount() <= 0) 
	        throw new InvalidAmountException("Transaction amount must be greater than zero");

	    Transaction transaction = new Transaction();
	    transaction.setCard(card);
	    transaction.setTransactionType(request.getTransactionType());
	    transaction.setAmount(request.getAmount());
	    transaction.setTransactionDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata"))
	    );
	    transaction.setDescription(request.getDescription());
	    transaction.setMerchant(request.getMerchant());

	    switch (request.getTransactionType()) {
	
	        case PURCHASE, CASH_WITHDRAWAL, FEE:
	            if (request.getAmount() > card.getAvailableLimit()) 
	            	throw new InsufficientCreditLimitException("Insufficient available credit limit!");
	
	            card.setAvailableLimit(card.getAvailableLimit() - request.getAmount());
	            card.setOutstandingBalance(card.getOutstandingBalance() + request.getAmount());
	            break;
	
	        case REFUND:
	
	        	if (request.getAmount() > card.getOutstandingBalance()) 
	                throw new InvalidAmountException("Refund amount cannot exceed outstanding balance");
	            
	            card.setAvailableLimit(card.getAvailableLimit() + request.getAmount());
	            card.setOutstandingBalance(card.getOutstandingBalance() - request.getAmount());
	            break;
	    }

	    creditCardDetailsRepository.save(card);

	    Transaction savedTransaction = transactionRepository.save(transaction);
	    TransactionResponse response = new TransactionResponse();

	    response.setTransactionId(savedTransaction.getTransactionId());
	    response.setCardNumber(savedTransaction.getCard().getCardNumber());
	    response.setTransactionType(savedTransaction.getTransactionType());
	    response.setAmount(savedTransaction.getAmount());
	    response.setTransactionDate(savedTransaction.getTransactionDate());
	    response.setDescription(savedTransaction.getDescription());
	    response.setMerchant(savedTransaction.getMerchant());

	    return response;
	}

	@Override
	public List<TransactionResponse> getTransactionsByCardNumber(String cardNumber) {

	    CreditCardDetails card = creditCardDetailsRepository.findById(cardNumber)
	            .orElseThrow(() -> new CreditCardNotFoundException("Credit card not available for this card number"));

	    List<Transaction> transactions = transactionRepository.findByCardCardNumber(card.getCardNumber());
	    List<TransactionResponse> responseList = new ArrayList<>();

	    for (Transaction transaction : transactions) {

	        TransactionResponse response = new TransactionResponse();

	        response.setTransactionId(transaction.getTransactionId());
	        response.setCardNumber(transaction.getCard().getCardNumber());
	        response.setTransactionType(transaction.getTransactionType());
	        response.setAmount(transaction.getAmount());
	        response.setTransactionDate(transaction.getTransactionDate());
	        response.setDescription(transaction.getDescription());
	        response.setMerchant(transaction.getMerchant());
	        responseList.add(response);
	    }

	    return responseList;
	}

	@Override
	public TransactionResponse getTransactionById(UUID transactionId) {

	    Transaction transaction = transactionRepository.findById(transactionId)
	            .orElseThrow(() -> new TransactionNotFoundException("Transaction not found for ID: " + transactionId));

	    TransactionResponse response = new TransactionResponse();

	    response.setTransactionId(transaction.getTransactionId());
	    response.setCardNumber(transaction.getCard().getCardNumber());
	    response.setTransactionType(transaction.getTransactionType());
	    response.setAmount(transaction.getAmount());
	    response.setTransactionDate(transaction.getTransactionDate());
	    response.setDescription(transaction.getDescription());
	    response.setMerchant(transaction.getMerchant());

	    return response;
	}

	@Override
	public List<TransactionResponse> getTransactionsByCustomerId(Long customerId) {

	    if (customerId == null || customerId <= 0 || customerId > Integer.MAX_VALUE)
	        throw new IllegalArgumentException("Customer ID must be a valid positive integer");

	    customerRepository.findById(customerId.intValue())
	            .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

	    List<Transaction> transactions = transactionRepository.findByCardCustomerCustomerId(customerId);

	    List<TransactionResponse> responseList = new ArrayList<>();

	    for (Transaction transaction : transactions) {

	        TransactionResponse response = new TransactionResponse();

	        response.setTransactionId(transaction.getTransactionId());
	        response.setCardNumber(transaction.getCard().getCardNumber());
	        response.setTransactionType(transaction.getTransactionType());
	        response.setAmount(transaction.getAmount());
	        response.setTransactionDate(transaction.getTransactionDate());
	        response.setDescription(transaction.getDescription());
	        response.setMerchant(transaction.getMerchant());

	        responseList.add(response);
	    }
	    return responseList;
	}

	@Override
	public List<TransactionResponse> filterTransactions(
	        String cardNumber,
	        String type,
	        LocalDate fromDate,
	        LocalDate toDate,
	        Double minAmount,
	        Double maxAmount) {

	    CreditCardDetails card = creditCardDetailsRepository.findById(cardNumber)
	            .orElseThrow(() -> new CreditCardNotFoundException(
	                    "Credit card not found for card number: " + cardNumber));

	    List<Transaction> transactions =
	            transactionRepository.findByCardCardNumber(card.getCardNumber());

	    if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
	        throw new InvalidDateRangeException(
	                "From date cannot be after to date");
	    }

	    if (minAmount != null && maxAmount != null && minAmount > maxAmount) {
	        throw new InvalidAmountException(
	                "Minimum amount cannot be greater than maximum amount");
	    }

	    List<TransactionResponse> responseList = new ArrayList<>();

	    for (Transaction transaction : transactions) {

	        boolean matchesType =
	                type == null
	                || type.isBlank()
	                || transaction.getTransactionType()
	                        .name()
	                        .equalsIgnoreCase(type);

	        boolean matchesFromDate =
	                fromDate == null
	                || !transaction.getTransactionDate()
	                        .toLocalDate()
	                        .isBefore(fromDate);

	        boolean matchesToDate =
	                toDate == null
	                || !transaction.getTransactionDate()
	                        .toLocalDate()
	                        .isAfter(toDate);

	        boolean matchesMinAmount =
	                minAmount == null
	                || transaction.getAmount() >= minAmount;

	        boolean matchesMaxAmount =
	                maxAmount == null
	                || transaction.getAmount() <= maxAmount;

	        if (matchesType
	                && matchesFromDate
	                && matchesToDate
	                && matchesMinAmount
	                && matchesMaxAmount) {

	            TransactionResponse response = new TransactionResponse();

	            response.setTransactionId(transaction.getTransactionId());
	            response.setCardNumber(transaction.getCard().getCardNumber());
	            response.setTransactionType(transaction.getTransactionType());
	            response.setAmount(transaction.getAmount());
	            response.setTransactionDate(transaction.getTransactionDate());
	            response.setDescription(transaction.getDescription());
	            response.setMerchant(transaction.getMerchant());

	            responseList.add(response);
	        }
	    }

	    return responseList;
	}
	@Override
	public TransactionSummaryResponse getTransactionSummary(String cardNumber) {
	    CreditCardDetails card = creditCardDetailsRepository.findById(cardNumber)
	            .orElseThrow(() -> new CreditCardNotFoundException("Credit card not found for card number: " + cardNumber));

	    List<Transaction> transactions = transactionRepository.findByCardCardNumber(card.getCardNumber());

	    long purchaseCount = 0;
	    long cashWithdrawalCount = 0;
	    long feeCount = 0;
	    long refundCount = 0;

	    double purchaseAmount = 0.0;
	    double cashWithdrawalAmount = 0.0;
	    double feeAmount = 0.0;
	    double refundAmount = 0.0;

	    for (Transaction transaction : transactions) {

	        switch (transaction.getTransactionType()) {
	            case PURCHASE:
	                purchaseCount++;
	                purchaseAmount += transaction.getAmount();
	                break;

	            case CASH_WITHDRAWAL:
	                cashWithdrawalCount++;
	                cashWithdrawalAmount += transaction.getAmount();
	                break;

	            case FEE:
	                feeCount++;
	                feeAmount += transaction.getAmount();
	                break;

	            case REFUND:
	                refundCount++;
	                refundAmount += transaction.getAmount();
	                break;
	        }
	    }

	    double totalAmount = purchaseAmount + cashWithdrawalAmount + feeAmount - refundAmount;

	    TransactionSummaryResponse response = new TransactionSummaryResponse();

	    response.setCardNumber(cardNumber);
	    response.setTotalTransactions((long) transactions.size());
	    response.setTotalTransactionAmount(totalAmount);

	    response.setPurchaseCount(purchaseCount);
	    response.setPurchaseAmount(purchaseAmount);

	    response.setCashWithdrawalCount(cashWithdrawalCount);
	    response.setCashWithdrawalAmount(cashWithdrawalAmount);

	    response.setFeeCount(feeCount);
	    response.setFeeAmount(feeAmount);

	    response.setRefundCount(refundCount);
	    response.setRefundAmount(refundAmount);

	    return response;
	}

}
