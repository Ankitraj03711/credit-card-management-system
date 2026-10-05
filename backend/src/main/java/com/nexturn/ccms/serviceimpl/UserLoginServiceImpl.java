package com.nexturn.ccms.serviceimpl;

import com.nexturn.ccms.dto.MessageResponse;
import com.nexturn.ccms.dto.UserLoginRequest;
import com.nexturn.ccms.dto.UserLoginResponse;
import com.nexturn.ccms.dto.UserRegisterRequest;
import com.nexturn.ccms.entity.UserLogin;
import com.nexturn.ccms.enums.UserRole;
import com.nexturn.ccms.exception.DuplicateEmailException;
import com.nexturn.ccms.exception.InvalidCredentialsException;
import com.nexturn.ccms.repository.UserLoginRepository;
import com.nexturn.ccms.service.UserLoginService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserLoginServiceImpl
        implements UserLoginService {

    private final UserLoginRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserLoginServiceImpl(
            UserLoginRepository repository,
            PasswordEncoder passwordEncoder) {

        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public MessageResponse register(
            UserRegisterRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                "Request cannot be null"
            );
        }

        if (request.getEmail() == null ||
            request.getEmail().isBlank()) {

            throw new IllegalArgumentException(
                "Email is required"
            );
        }

        if (request.getPassword() == null ||
            request.getPassword().isBlank()) {

            throw new IllegalArgumentException(
                "Password is required"
            );
        }

        String email =
                request.getEmail().trim();

        if (repository.existsByEmail(email)) {

            throw new DuplicateEmailException(
                "User email already exists: "
                + email
            );
        }

        UserLogin user = new UserLogin(
            email,
            passwordEncoder.encode(request.getPassword()),
            UserRole.CUSTOMER
        );

        repository.save(user);

        return new MessageResponse(
            "User registered successfully"
        );
    }

    @Override
    @Transactional
    public UserLoginResponse login(
            UserLoginRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                "Request cannot be null"
            );
        }

        if (request.getEmail() == null ||
            request.getEmail().isBlank()) {

            throw new IllegalArgumentException(
                "Email is required"
            );
        }

        if (request.getPassword() == null ||
            request.getPassword().isBlank()) {

            throw new IllegalArgumentException(
                "Password is required"
            );
        }

        String email =
                request.getEmail().trim();

        UserLogin user =
            repository.findByEmail(email)
                .orElseThrow(() ->
                    new InvalidCredentialsException(
                        "Invalid email or password"
                    ));

        String storedPassword = user.getPassword();
        boolean passwordMatches;

        if (isBcryptHash(storedPassword)) {

            passwordMatches = passwordEncoder.matches(
                    request.getPassword(),
                    storedPassword
            );

        } else {
        	// OLD CODE - SonarLint issue: storedPassword may be null
        	// passwordMatches = storedPassword.equals(
        	//        	         request.getPassword()
        	// );

            passwordMatches = storedPassword != null
                    && storedPassword.equals(request.getPassword());

            if (passwordMatches) {
                user.setPassword(
                        passwordEncoder.encode(request.getPassword())
                );
                repository.save(user);
            }
        }

        if (!passwordMatches) {
            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }

   

        return new UserLoginResponse(
            "Login successful",
            user.getEmail(),
            user.getRole().name()
        );
    }

    private boolean isBcryptHash(String value) {
        return value != null
                && value.matches("^\\$2[aby]\\$\\d{2}\\$.*$");
    }
}