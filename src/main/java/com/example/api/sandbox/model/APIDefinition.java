package com.example.api.sandbox.model;

import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;

import com.example.api.sandbox.exception.PathNotFoundException;
import com.example.api.sandbox.exception.RequestNotFoundException;

/**
 * 
 * 
 * @since v1
 */
public interface APIDefinition {

	/**
	 * 
	 * @param httpRequest
	 * @throws PathNotFoundException
	 */
	CompletableFuture<RequestResponse> processRequest(final HttpServletRequest httpServletRequest)
			throws RequestNotFoundException;

}
