package com.test.smartcard.exception;

public class CardDataException extends RuntimeException{
	private static final long serialVersionUID = 1L;
	public CardDataException(String message){
		super(message);
	}
}
