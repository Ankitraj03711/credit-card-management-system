package com.nexturn.ccms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;

import com.nexturn.ccms.entity.CreditCardApplication;

public interface CreditCardApplicationRepository
        extends JpaRepository<CreditCardApplication, String> {

    List<CreditCardApplication> findByCustomerCustomerIdOrderByApplicationDateDesc(
            Integer customerId);

    List<CreditCardApplication> findAllByOrderByApplicationDateDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CreditCardApplication> findLockedByApplicationId(String applicationId);
}
