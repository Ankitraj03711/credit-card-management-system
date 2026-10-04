package com.nexturn.ccms.repository;

import com.nexturn.ccms.entity.Address;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AddressRepository
        extends JpaRepository<Address, UUID> {

    List<Address> findByCustomer_CustomerId(
            Integer customerId);
}