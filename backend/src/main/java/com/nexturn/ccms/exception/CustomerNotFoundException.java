package com.nexturn.ccms.exception;

public class CustomerNotFoundException
        extends RuntimeException {

    public CustomerNotFoundException(
            String message) {

        super(message);
    }
}