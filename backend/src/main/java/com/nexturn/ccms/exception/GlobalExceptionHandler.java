package com.nexturn.ccms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
        CustomerNotFoundException.class,
        UserNotFoundException.class,
        AddressNotFoundException.class,
        CardTypeNotFoundException.class,
        CreditCardNotFoundException.class,
        PaymentNotFoundException.class,
        CreditCardApplicationNotFoundException.class,
        TransactionNotFoundException.class
    })
    public ResponseEntity<Map<String, Object>>
    handleNotFound(RuntimeException exception) {

        return createResponse(
            HttpStatus.NOT_FOUND,
            exception.getMessage()
        );
    }

    @ExceptionHandler(
        DuplicateEmailException.class
    )
    public ResponseEntity<Map<String, Object>>
    handleDuplicateEmail(
            DuplicateEmailException exception) {

        return createResponse(
            HttpStatus.CONFLICT,
            exception.getMessage()
        );
    }

    @ExceptionHandler(
        InvalidCredentialsException.class
    )
    public ResponseEntity<Map<String, Object>>
    handleInvalidCredentials(
            InvalidCredentialsException exception) {

        return createResponse(
            HttpStatus.UNAUTHORIZED,
            exception.getMessage()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException exception) {
        return createResponse(HttpStatus.FORBIDDEN, "Access denied");
    }

    @ExceptionHandler(
        IllegalArgumentException.class
    )
    public ResponseEntity<Map<String, Object>>
    handleBadRequest(
            IllegalArgumentException exception) {

        return createResponse(
            HttpStatus.BAD_REQUEST,
            exception.getMessage()
        );
    }

    @ExceptionHandler({
        InsufficientCreditLimitException.class,
        InvalidAmountException.class,
        InvalidCardStatusException.class,
        InvalidDateRangeException.class
    })
    public ResponseEntity<Map<String, Object>>
    handleDomainValidation(RuntimeException exception) {

        return createResponse(
            HttpStatus.BAD_REQUEST,
            exception.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>>
    handleGeneral(Exception exception) {

        return createResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal server error"
        );
    }

    private ResponseEntity<Map<String, Object>>
    createResponse(
            HttpStatus status,
            String message) {

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
            "status",
            status.value()
        );

        response.put(
            "message",
            message
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }
}