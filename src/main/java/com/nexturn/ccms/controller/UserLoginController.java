package com.nexturn.ccms.controller;

import com.nexturn.ccms.dto.MessageResponse;
import com.nexturn.ccms.dto.UserLoginRequest;
import com.nexturn.ccms.dto.UserLoginResponse;
import com.nexturn.ccms.dto.UserRegisterRequest;
import com.nexturn.ccms.service.UserLoginService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserLoginController {

    private final UserLoginService service;

    public UserLoginController(
            UserLoginService service) {

        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse>
    register(
            @RequestBody UserRegisterRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse>
    login(
            @RequestBody UserLoginRequest request) {

        return ResponseEntity.ok(
            service.login(request)
        );
    }
}