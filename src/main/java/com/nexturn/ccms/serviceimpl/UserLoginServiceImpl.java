package com.nexturn.ccms.serviceimpl;

import com.nexturn.ccms.dto.MessageResponse;
import com.nexturn.ccms.dto.UserLoginRequest;
import com.nexturn.ccms.dto.UserLoginResponse;
import com.nexturn.ccms.dto.UserRegisterRequest;
import com.nexturn.ccms.entity.UserLogin;
import com.nexturn.ccms.exception.DuplicateEmailException;
import com.nexturn.ccms.exception.UserNotFoundException;
import com.nexturn.ccms.repository.UserLoginRepository;
import com.nexturn.ccms.service.UserLoginService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserLoginServiceImpl
        implements UserLoginService {

    private final UserLoginRepository repository;

    public UserLoginServiceImpl(
            UserLoginRepository repository) {

        this.repository = repository;
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

        if (request.getRole() == null) {

            throw new IllegalArgumentException(
                "Role is required"
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
            request.getPassword(),
            request.getRole()
        );

        repository.save(user);

        return new MessageResponse(
            "User registered successfully"
        );
    }

    @Override
    @Transactional(readOnly = true)
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
                    new UserNotFoundException(
                        "Invalid email or password"
                    ));

        if (!user.getPassword()
                .equals(request.getPassword())) {

            throw new UserNotFoundException(
                "Invalid email or password"
            );
        }

        return new UserLoginResponse(
            "Login successful",
            user.getEmail(),
            user.getRole().name()
        );
    }
}