package com.example.api.sandbox.model;

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
	Object processRequest(final HttpServletRequest httpServletRequest) throws RequestNotFoundException;
	
}
