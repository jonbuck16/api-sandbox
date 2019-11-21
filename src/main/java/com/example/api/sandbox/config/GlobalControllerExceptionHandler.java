package com.example.api.sandbox.config;

import java.util.LinkedList;
import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.example.api.sandbox.exception.InternalServerException;
import com.example.api.sandbox.exception.InvalidInputException;
import com.example.api.sandbox.exception.EndpointNotFoundException;
import com.example.api.sandbox.exception.RequestNotProcessedException;
import com.example.api.sandbox.model.ApiError;
import com.example.api.sandbox.model.ApiSubError;
import com.example.api.sandbox.model.ApiValidationError;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class GlobalControllerExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(EndpointNotFoundException.class)
	protected ResponseEntity<Object> handleRequestNotFoundException(EndpointNotFoundException ex) {
		return buildResponseEntity(new ApiError(HttpStatus.BAD_REQUEST, "Endpoint Not Found!", ex));
	}

	@ExceptionHandler(InvalidInputException.class)
	protected ResponseEntity<Object> handleInvalidInputException(InvalidInputException ex) {
		final ApiError apiError = new ApiError(HttpStatus.valueOf(ex.getStatus()), "Invalid Input!", ex);
		List<ApiSubError> subErrors = new LinkedList<>();
		ex.getMissingParameters().forEach(s -> subErrors.add(new ApiValidationError(s, "Value missing!")));
		apiError.setSubErrors(subErrors);
		return buildResponseEntity(apiError);
	}
	
	@ExceptionHandler(RequestNotProcessedException.class)
	protected ResponseEntity<Object> handleRequestNotProcessedException(EndpointNotFoundException ex) {
		return buildResponseEntity(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Request Not Processed!", ex));
	}
	
	@ExceptionHandler(InternalServerException.class)
	protected ResponseEntity<Object> handleInternalServerError(InternalServerException ex) {
	    return buildResponseEntity(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex));
	}
	
	private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
		return new ResponseEntity<>(apiError, apiError.getStatus());
	}

}
