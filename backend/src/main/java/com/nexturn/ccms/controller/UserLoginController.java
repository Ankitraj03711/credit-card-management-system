package com.nexturn.ccms.controller;

import com.nexturn.ccms.dto.MessageResponse;
import com.nexturn.ccms.dto.UserLoginRequest;
import com.nexturn.ccms.dto.UserLoginResponse;
import com.nexturn.ccms.dto.UserRegisterRequest;
import com.nexturn.ccms.service.UserLoginService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/auth")
public class UserLoginController {

    private final UserLoginService service;
    private final SecurityContextRepository securityContextRepository;

    public UserLoginController(
            UserLoginService service,
            SecurityContextRepository securityContextRepository) {

        this.service = service;
        this.securityContextRepository = securityContextRepository;
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
            @RequestBody UserLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        UserLoginResponse response = service.login(request);
        httpRequest.getSession(true);
        httpRequest.changeSessionId();

        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                response.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority(
                        "ROLE_" + response.getRole())));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(
                context,
                httpRequest,
                httpResponse);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/csrf")
    public ResponseEntity<Map<String, String>> csrfToken(
            CsrfToken csrfToken) {
        return ResponseEntity.ok(Map.of("token", csrfToken.getToken()));
    }
}