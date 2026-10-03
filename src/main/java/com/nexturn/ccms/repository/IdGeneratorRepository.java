package com.nexturn.ccms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexturn.ccms.entity.IdGenerator;

public interface IdGeneratorRepository extends JpaRepository<IdGenerator, String> {

}