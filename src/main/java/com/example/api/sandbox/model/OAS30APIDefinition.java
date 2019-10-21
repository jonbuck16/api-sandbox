package com.example.api.sandbox.model;

import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;

import com.example.api.sandbox.exception.RequestNotFoundException;

import io.swagger.v3.oas.models.OpenAPI;
import lombok.Getter;

/**
 * 
 * 
 * @since v1
 */
public class OAS30APIDefinition extends AbstractAPIDefinition {

	@Getter
	private OpenAPI openAPI;

	public OAS30APIDefinition(final OpenAPI openApi) {
		super(ModelType.OAS3);
		this.openAPI = openApi;
	}

	@Override
	public CompletableFuture<RequestResponse> processRequest(HttpServletRequest httpServletRequest) throws RequestNotFoundException {
		return CompletableFuture.completedFuture(RequestResponse.EMPTY);
	}

}
