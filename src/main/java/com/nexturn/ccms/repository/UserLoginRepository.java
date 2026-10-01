package com.nexturn.ccms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexturn.ccms.entity.UserLogin;

public interface UserLoginRepository extends JpaRepository<UserLogin, String> {

}