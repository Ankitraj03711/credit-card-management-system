package com.nexturn.ccms.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.nexturn.ccms.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, String> {

}