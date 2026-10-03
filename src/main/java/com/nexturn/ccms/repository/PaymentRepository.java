package com.nexturn.ccms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexturn.ccms.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, String> {
	
	Optional<Payment> findByPaymentReference(String paymentReference);
	List<Payment> findByCardCardNumber(String cardNumber);
	List<Payment> findByCardCustomerCustomerId(Integer customerId);
}