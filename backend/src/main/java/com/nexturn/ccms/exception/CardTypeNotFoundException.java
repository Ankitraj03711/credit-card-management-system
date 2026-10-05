package com.nexturn.ccms.exception;

public class CardTypeNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public CardTypeNotFoundException(String message) {
        super(message);
    }
}
