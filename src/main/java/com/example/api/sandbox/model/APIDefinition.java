package com.example.api.sandbox.model;

import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;

import com.example.api.sandbox.exception.DefinitionNotFoundException;
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
	 * @throws DefinitionNotFoundException
	 */
	CompletableFuture<RequestResponse> processRequest(final HttpServletRequest httpServletRequest)
			throws RequestNotFoundException;

}
