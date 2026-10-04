package com.nexturn.ccms.exception;

public class InsufficientCreditLimitException extends RuntimeException{
	
	
	private static final long serialVersionUID = 1L;

	public InsufficientCreditLimitException(String message) {
		super(message);
	}
}
