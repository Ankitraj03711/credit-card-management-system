package com.nexturn.ccms.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexturn.ccms.entity.Address;

public interface AddressRepository extends JpaRepository<Address, UUID> {

}
