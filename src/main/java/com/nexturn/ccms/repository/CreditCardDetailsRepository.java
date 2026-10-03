package com.nexturn.ccms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexturn.ccms.entity.CreditCardDetails;

public interface CreditCardDetailsRepository
        extends JpaRepository<CreditCardDetails, String> {

    List<CreditCardDetails> findByCustomerCustomerId(
            Integer customerId);
}