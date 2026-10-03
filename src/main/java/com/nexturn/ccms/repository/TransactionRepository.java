package com.nexturn.ccms.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexturn.ccms.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
	List<Transaction> findByCardCardNumber(String cardNumber);
	List<Transaction> findByCardCustomerCustomerId(Long customerId);
}
