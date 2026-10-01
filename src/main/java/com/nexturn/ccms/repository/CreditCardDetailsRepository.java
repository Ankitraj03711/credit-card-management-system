package com.nexturn.ccms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexturn.ccms.entity.CreditCardDetails;

public interface CreditCardDetailsRepository extends JpaRepository<CreditCardDetails, String> {

}