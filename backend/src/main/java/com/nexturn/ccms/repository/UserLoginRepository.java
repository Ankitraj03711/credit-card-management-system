package com.nexturn.ccms.repository;

import com.nexturn.ccms.entity.UserLogin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserLoginRepository
        extends JpaRepository<UserLogin, String> {

    Optional<UserLogin> findByEmail(String email);

    boolean existsByEmail(String email);
}