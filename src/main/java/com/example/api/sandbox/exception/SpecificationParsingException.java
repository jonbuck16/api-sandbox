package com.example.api.sandbox.exception;

public class SpecificationParsingException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3430249980500819324L;

	public SpecificationParsingException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public SpecificationParsingException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SpecificationParsingException(String message, Throwable cause) {
		super(message, cause);
	}

	public SpecificationParsingException(String message) {
		super(message);
	}

	public SpecificationParsingException(Throwable cause) {
		super(cause);
	}

}
