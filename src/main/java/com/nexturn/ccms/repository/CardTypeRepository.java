package com.nexturn.ccms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexturn.ccms.entity.CardType;

public interface CardTypeRepository
        extends JpaRepository<CardType, Integer> {
}