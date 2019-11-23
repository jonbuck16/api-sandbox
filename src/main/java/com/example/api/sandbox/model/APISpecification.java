package com.example.api.sandbox.model;

import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;

import com.example.api.sandbox.exception.PathNotFoundException;
import com.example.api.sandbox.exception.EndpointNotFoundException;

/**
 * 
 * 
 * @since v1
 */
public interface APISpecification {

	/**
	 * 
	 * @param httpRequest
	 * @throws PathNotFoundException
	 */
	CompletableFuture<RequestResponse> processRequest(final HttpServletRequest httpServletRequest)
			throws EndpointNotFoundException;

}
