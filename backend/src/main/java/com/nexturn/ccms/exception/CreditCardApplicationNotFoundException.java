package com.nexturn.ccms.exception;

public class CreditCardApplicationNotFoundException
        extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public CreditCardApplicationNotFoundException(String message) {
        super(message);
    }
}
