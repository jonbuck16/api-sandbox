package com.example.api.sandbox.exception;

public class DefinitionParsingException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3430249980500819324L;

	public DefinitionParsingException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DefinitionParsingException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DefinitionParsingException(String message, Throwable cause) {
		super(message, cause);
	}

	public DefinitionParsingException(String message) {
		super(message);
	}

	public DefinitionParsingException(Throwable cause) {
		super(cause);
	}

}
