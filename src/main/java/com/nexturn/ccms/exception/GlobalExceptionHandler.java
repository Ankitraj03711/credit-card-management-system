package com.nexturn.ccms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
        CustomerNotFoundException.class,
        UserNotFoundException.class,
        AddressNotFoundException.class
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