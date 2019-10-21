package com.example.api.sandbox.exception;

public class RequestNotProcessedException extends RuntimeException {

	private static final long serialVersionUID = 4718647541129394033L;

	public RequestNotProcessedException() {
		super();
	}

	public RequestNotProcessedException(String message) {
		super(message);
	}
}
