package com.example.api.sandbox.model;

import lombok.Getter;

/**
 * 
 * 
 * @since v1
 */
public class ApiValidationError extends ApiSubError {
	@Getter private String field;
	@Getter private String message;

	public ApiValidationError(final String fieldValue, final String messageValue) {
		this.field = fieldValue;
		this.message = messageValue;
	}
}
