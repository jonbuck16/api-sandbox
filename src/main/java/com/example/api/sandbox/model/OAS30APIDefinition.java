package com.example.api.sandbox.model;

import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;

import com.example.api.sandbox.exception.RequestNotFoundException;

import io.swagger.v3.oas.models.OpenAPI;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * 
 * @since v1
 */
public class OAS30APIDefinition extends AbstractAPIDefinition {

	@Getter
	private OpenAPI openAPI;

	@Setter
	@Getter
	private OpenAPI openApi;
	
	public OAS30APIDefinition() {
		super(ModelType.OAS3);
	}

	@Override
	public CompletableFuture<RequestResponse> processRequest(HttpServletRequest httpServletRequest) throws RequestNotFoundException {
		return CompletableFuture.completedFuture(RequestResponse.EMPTY);
	}

}
