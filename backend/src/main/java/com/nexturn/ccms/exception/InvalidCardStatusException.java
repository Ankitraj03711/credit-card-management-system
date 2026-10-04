package com.nexturn.ccms.exception;

public class InvalidCardStatusException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;

	public InvalidCardStatusException(String message) {
		super(message);
	}
}
