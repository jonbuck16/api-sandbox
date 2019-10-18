package com.example.api.sandbox.exception;

import java.util.List;

import lombok.Getter;


public class InvalidInputException extends RuntimeException {

	private static final long serialVersionUID = -8027543878800859133L;
	
	@Getter
	private int status;
	
	@Getter
	private List<String> missingParameters;

	public InvalidInputException(final int status, final String message, final List<String> missingParameters) {
		super(message);
		this.status = status;
		this.missingParameters = missingParameters;
	}
	
}
