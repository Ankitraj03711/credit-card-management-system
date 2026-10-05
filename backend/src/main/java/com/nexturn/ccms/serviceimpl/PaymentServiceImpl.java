package com.nexturn.ccms.serviceimpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nexturn.ccms.dto.PaymentRequest;
import com.nexturn.ccms.dto.PaymentResponse;
import com.nexturn.ccms.dto.PaymentSummaryResponse;
import com.nexturn.ccms.entity.CreditCardDetails;
import com.nexturn.ccms.entity.Payment;
import com.nexturn.ccms.enums.PaymentStatus;
import com.nexturn.ccms.exception.CreditCardNotFoundException;
import com.nexturn.ccms.exception.CustomerNotFoundException;
import com.nexturn.ccms.exception.InvalidAmountException;
import com.nexturn.ccms.exception.InvalidDateRangeException;
import com.nexturn.ccms.exception.PaymentNotFoundException;
import com.nexturn.ccms.repository.CreditCardDetailsRepository;
import com.nexturn.ccms.repository.CustomerRepository;
import com.nexturn.ccms.repository.PaymentRepository;
import com.nexturn.ccms.service.IdGeneratorService;
import com.nexturn.ccms.service.PaymentService;

import jakarta.transaction.Transactional;

@Service
public class PaymentServiceImpl implements PaymentService {

	private static final String CARD_NOT_FOUND = "Credit card not found with card number: ";
	private final PaymentRepository paymentRepository;
	private final CreditCardDetailsRepository creditCardDetailsRepository;
	private final IdGeneratorService idGeneratorService;
	private final CustomerRepository customerRepository;

	public PaymentServiceImpl(
	        PaymentRepository paymentRepository,
	        CreditCardDetailsRepository creditCardDetailsRepository,
	        IdGeneratorService idGeneratorService,
	        CustomerRepository customerRepository) {

	    this.paymentRepository = paymentRepository;
	    this.creditCardDetailsRepository = creditCardDetailsRepository;
	    this.idGeneratorService = idGeneratorService;
	    this.customerRepository = customerRepository;
	}
	
	
	@Override
	@Transactional
	public PaymentResponse createPayment(PaymentRequest request) {

	    CreditCardDetails card = creditCardDetailsRepository.findById(request.getCardNumber())
	            .orElseThrow(() -> new CreditCardNotFoundException(
	                    "Credit card not found for card number: " + request.getCardNumber()));

	     if (request.getAmount() == null || request.getAmount() <= 0)
	        throw new InvalidAmountException("Payment amount must be greater than zero");
	    

	    if (request.getAmount() > card.getOutstandingBalance())
	        throw new InvalidAmountException(
	                "Payment amount cannot exceed outstanding balance");

	    String paymentReference = "PAY-" + LocalDate.now(ZoneId.of("Asia/Kolkata")) +"-"+ idGeneratorService.getNextValue("PAYMENT_REFERENCE");

	    Payment payment = new Payment();

	    payment.setPaymentReference(paymentReference);
	    payment.setCard(card);
	    payment.setAmount(request.getAmount());
	    payment.setPaymentDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
	    payment.setPaymentMode(request.getPaymentMode());
	    payment.setPaymentStatus(PaymentStatus.SUCCESS);
	    payment.setDescription(request.getDescription());

	    card.setOutstandingBalance(card.getOutstandingBalance() - request.getAmount());
	    card.setAvailableLimit(card.getAvailableLimit() + request.getAmount());

	    creditCardDetailsRepository.save(card);

	    Payment savedPayment = paymentRepository.save(payment);
	    PaymentResponse response = new PaymentResponse();

	    response.setPaymentReference(savedPayment.getPaymentReference());
	    response.setCardNumber(savedPayment.getCard().getCardNumber());
	    response.setAmount(savedPayment.getAmount());
	    response.setPaymentDate(savedPayment.getPaymentDate());
	    response.setPaymentMode(savedPayment.getPaymentMode());
	    response.setPaymentStatus(savedPayment.getPaymentStatus());
	    response.setDescription(savedPayment.getDescription());
	    
	    return response;
	}

	@Override
	public PaymentResponse getPaymentByReference(String paymentReference) {

	    if (paymentReference == null || paymentReference.trim().isEmpty())
	        throw new IllegalArgumentException("Payment reference is required");
	    
	    Payment payment = paymentRepository
	            .findByPaymentReference(paymentReference)
	            .orElseThrow(() -> new PaymentNotFoundException("Payment not found with reference: " + paymentReference));

	    PaymentResponse response = new PaymentResponse();

	    response.setPaymentReference(payment.getPaymentReference());
	    response.setCardNumber(payment.getCard().getCardNumber());
	    response.setAmount(payment.getAmount());
	    response.setPaymentMode(payment.getPaymentMode());
	    response.setPaymentDate(payment.getPaymentDate());
	    response.setDescription(payment.getDescription());
	    response.setPaymentStatus(payment.getPaymentStatus());

	    return response;
	}

	@Override
	public List<PaymentResponse> getPaymentsByCardNumber(String cardNumber) {

	    if (cardNumber == null || cardNumber.trim().isEmpty()) 
	        throw new IllegalArgumentException("Card number is required");
	
	    CreditCardDetails card = creditCardDetailsRepository
	            .findById(cardNumber)
	            .orElseThrow(() -> new CreditCardNotFoundException(CARD_NOT_FOUND + cardNumber));

	    List<Payment> payments = paymentRepository.findByCardCardNumber(card.getCardNumber());

	    List<PaymentResponse> responses = new ArrayList<>();

	    for (Payment payment : payments) {

	        PaymentResponse response = new PaymentResponse();
	        response.setPaymentReference(payment.getPaymentReference());
	        response.setCardNumber(payment.getCard().getCardNumber());
	        response.setAmount(payment.getAmount());
	        response.setPaymentMode(payment.getPaymentMode());
	        response.setPaymentDate(payment.getPaymentDate());
	        response.setDescription(payment.getDescription());
	        response.setPaymentStatus(payment.getPaymentStatus());

	        responses.add(response);
	    }
	    return responses;
	}

	@Override
	public List<PaymentResponse> getPaymentsByCustomerId(Integer customerId) {

	    // Validate customer ID
	    if (customerId == null || customerId <= 0)
	        throw new IllegalArgumentException("Customer ID must be greater than zero");
	   
	    customerRepository.findById(customerId)
	            .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

	    List<Payment> payments = paymentRepository.findByCardCustomerCustomerId(customerId);

	    List<PaymentResponse> responses = new ArrayList<>();

	    for (Payment payment : payments) {

	        PaymentResponse response = new PaymentResponse();

	        response.setPaymentReference(payment.getPaymentReference());
	        response.setCardNumber(payment.getCard().getCardNumber());
	        response.setAmount(payment.getAmount());
	        response.setPaymentMode(payment.getPaymentMode());
	        response.setPaymentDate(payment.getPaymentDate());
	        response.setDescription(payment.getDescription());
	        response.setPaymentStatus(payment.getPaymentStatus());

	        responses.add(response);
	    }

	    return responses;
	}

	@Override
	public List<PaymentResponse> filterPayments(
	        String cardNumber,
	        String mode,
	        String status,
	        LocalDate fromDate,
	        LocalDate toDate,
	        Double minAmount,
	        Double maxAmount) {
	
	    if (cardNumber == null || cardNumber.trim().isEmpty()) {
	        throw new IllegalArgumentException("Card number is required");
	    }
	
	    creditCardDetailsRepository.findById(cardNumber)
	            .orElseThrow(() -> new CreditCardNotFoundException(
	                    CARD_NOT_FOUND + cardNumber));
	
	    if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
	        throw new InvalidDateRangeException(
	                "From date cannot be after to date");
	    }
	
	    if (minAmount != null && minAmount < 0) {
	        throw new IllegalArgumentException(
	                "Minimum amount cannot be negative");
	    }
	
	    if (maxAmount != null && maxAmount < 0) {
	        throw new IllegalArgumentException(
	                "Maximum amount cannot be negative");
	    }
	
	    if (minAmount != null
	            && maxAmount != null
	            && minAmount > maxAmount) {
	
	        throw new IllegalArgumentException(
	                "Minimum amount cannot be greater than maximum amount");
	    }
	
	    List<Payment> payments =
	            paymentRepository.findByCardCardNumber(cardNumber);
	
	    if (payments.isEmpty()) {
	        throw new PaymentNotFoundException(
	                "No payments found for card number: " + cardNumber);
	    }
	
	    List<PaymentResponse> responses = new ArrayList<>();
	
	    for (Payment payment : payments) {
	
	        if (matchesPayment(
	                payment,
	                mode,
	                status,
	                fromDate,
	                toDate,
	                minAmount,
	                maxAmount)) {
	
	            PaymentResponse response = new PaymentResponse();
	
	            response.setPaymentReference(
	                    payment.getPaymentReference());
	            response.setCardNumber(
	                    payment.getCard().getCardNumber());
	            response.setAmount(payment.getAmount());
	            response.setPaymentMode(payment.getPaymentMode());
	            response.setPaymentDate(payment.getPaymentDate());
	            response.setDescription(payment.getDescription());
	            response.setPaymentStatus(payment.getPaymentStatus());
	
	            responses.add(response);
	        }
	    }
	
	    if (responses.isEmpty()) {
	        throw new PaymentNotFoundException(
	                "No payments found matching the given filters "
	                        + "for card number: " + cardNumber);
	    }
	
	    return responses;
	}

	@Override
	public PaymentSummaryResponse getPaymentSummary(String cardNumber) {

	    if (cardNumber == null || cardNumber.trim().isEmpty())
	        throw new IllegalArgumentException("Card number is required");
	    

	    
	    creditCardDetailsRepository.findById(cardNumber)
	            .orElseThrow(() -> new CreditCardNotFoundException(CARD_NOT_FOUND + cardNumber));

	    List<Payment> payments = paymentRepository.findByCardCardNumber(cardNumber);

	    long totalPayments = payments.size();
	    Double totalPaidAmount = 0.0;
	    Double lastPaymentAmount = null;
	    LocalDateTime lastPaymentDate = null;

	    for (Payment payment : payments) {

	        if (payment.getAmount() != null) {
	            totalPaidAmount += payment.getAmount();
	        }

	        if (payment.getPaymentDate() != null
	                && (lastPaymentDate == null
	                        || payment.getPaymentDate().isAfter(lastPaymentDate))) {

	            lastPaymentDate = payment.getPaymentDate();
	            lastPaymentAmount = payment.getAmount();
	        }
	    }

	    PaymentSummaryResponse response = new PaymentSummaryResponse();

	    response.setTotalPayments(totalPayments);
	    response.setTotalPaidAmount(totalPaidAmount);
	    response.setLastPaymentAmount(lastPaymentAmount);
	    response.setLastPaymentDate(lastPaymentDate);

	    return response;
	}
	
	private boolean matchesPayment(
	        Payment payment,
	        String mode,
	        String status,
	        LocalDate fromDate,
	        LocalDate toDate,
	        Double minAmount,
	        Double maxAmount) {

	    boolean matchesMode =
	            mode == null
	            || mode.trim().isEmpty()
	            || (payment.getPaymentMode() != null
	                    && payment.getPaymentMode()
	                            .name()
	                            .equalsIgnoreCase(mode));

	    boolean matchesStatus =
	            status == null
	            || status.trim().isEmpty()
	            || (payment.getPaymentStatus() != null
	                    && payment.getPaymentStatus()
	                            .name()
	                            .equalsIgnoreCase(status));

	    boolean matchesFromDate =
	            fromDate == null
	            || (payment.getPaymentDate() != null
	                    && !payment.getPaymentDate()
	                            .toLocalDate()
	                            .isBefore(fromDate));

	    boolean matchesToDate =
	            toDate == null
	            || (payment.getPaymentDate() != null
	                    && !payment.getPaymentDate()
	                            .toLocalDate()
	                            .isAfter(toDate));

	    boolean matchesMinAmount =
	            minAmount == null
	            || (payment.getAmount() != null
	                    && payment.getAmount() >= minAmount);

	    boolean matchesMaxAmount =
	            maxAmount == null
	            || (payment.getAmount() != null
	                    && payment.getAmount() <= maxAmount);

	    return matchesMode
	            && matchesStatus
	            && matchesFromDate
	            && matchesToDate
	            && matchesMinAmount
	            && matchesMaxAmount;
	}
	
}
