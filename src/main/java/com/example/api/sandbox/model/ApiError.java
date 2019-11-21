package com.example.api.sandbox.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * 
 * @since v1
 */
public class ApiError {

	@Getter @Setter private HttpStatus status;
	@Getter @Setter private LocalDateTime timestamp;
	@Getter @Setter private String type;
	@Getter @Setter private String message;
	@Getter @Setter private List<ApiSubError> subErrors;

	private ApiError() {
		timestamp = LocalDateTime.now();
	}

	public ApiError(HttpStatus status) {
		this();
		this.status = status;
	}

	public ApiError(HttpStatus status, Throwable ex) {
		this();
		this.status = status;
		this.type = "Unexpected Exception";
		this.message = ex.getLocalizedMessage();
	}

	public ApiError(final HttpStatus status, final String type, Throwable ex) {
		this();
		this.status = status;
		this.type = type;
		this.message = ex.getLocalizedMessage();
	}

}
