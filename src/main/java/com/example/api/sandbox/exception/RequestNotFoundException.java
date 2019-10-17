package com.example.api.sandbox.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Actor Not Found")
public class RequestNotFoundException extends Exception {

	private static final long serialVersionUID = -4612152836426847666L;

	public RequestNotFoundException(String message) {
		super(message);
	}
	
}
