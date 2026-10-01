package com.nexturn.ccms.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexturn.ccms.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

}