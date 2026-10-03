package com.nexturn.ccms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 404 - Credit Card Not Found
    @ExceptionHandler(CreditCardNotFoundException.class)
    public ResponseEntity<String> creditCardNotFoundException(CreditCardNotFoundException ex) {

        return new ResponseEntity<String>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // 400 - Insufficient Credit Limit
    @ExceptionHandler(InsufficientCreditLimitException.class)
    public ResponseEntity<String> insufficientCreditLimitException(InsufficientCreditLimitException ex) {

        return new ResponseEntity<String>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // 409 - Invalid Card Status
    @ExceptionHandler(InvalidCardStatusException.class)
    public ResponseEntity<String> invalidCardStatusException(InvalidCardStatusException ex) {

        return new ResponseEntity<String>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    // 400 - Invalid Amount
    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<String> invalidAmountException( InvalidAmountException ex) {

        return new ResponseEntity<String>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // 404 - Transaction Not Found
    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<String> transactionNotFoundException( TransactionNotFoundException ex) {

        return new ResponseEntity<String>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // 404 - Customer Not Found
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<String> customerNotFoundException(CustomerNotFoundException ex) {

        return new ResponseEntity<String>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // 400 - Invalid Date Range
    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<String> invalidDateRangeException(InvalidDateRangeException ex) {

        return new ResponseEntity<String>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // 400 - Invalid Argument
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> illegalArgumentException(IllegalArgumentException ex) {

        return new ResponseEntity<String>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // 404 - Payment Not Found
    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<String> paymentNotFoundException(PaymentNotFoundException ex) {

        return new ResponseEntity<String>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
}