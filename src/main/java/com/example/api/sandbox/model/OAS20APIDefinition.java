package com.example.api.sandbox.model;

import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import com.example.api.sandbox.exception.DefinitionNotFoundException;
import com.example.api.sandbox.exception.RequestNotFoundException;
import com.example.api.sandbox.utils.SwaggerPathUtils;

import io.swagger.models.HttpMethod;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import lombok.Getter;
import lombok.extern.flogger.Flogger;

/**
 * 
 * 
 * @since v1
 */
@Flogger
public class OAS20APIDefinition extends AbstractAPIDefinition {

	@Getter
	private Swagger swagger;

	public OAS20APIDefinition(final Swagger swagger) {
		super(ModelType.OAS2);
		this.swagger = swagger;
	}

	/**
	 * 
	 */
	@Override
	public void matchRequest(HttpServletRequest httpServletRequest) throws RequestNotFoundException {
		if (swagger.getPaths().isEmpty()) {
			throw new DefinitionNotFoundException();
		}

		for (Entry<String, Path> entry : swagger.getPaths().entrySet()) {
			if (SwaggerPathUtils.pathToRegex(entry.getValue(), entry.getKey())
					.matcher(httpServletRequest.getRequestURI()).matches()) {
				final HttpMethod httpMethod = HttpMethod.valueOf(httpServletRequest.getMethod());
				if (entry.getValue().getOperationMap().containsKey(httpMethod)) {
					log.atInfo().log("Request matched to path [%s] %s", httpMethod.name(), entry.getKey());
					return;
				}
			}
		}

		throw new RequestNotFoundException(String.format("The request [%s] %s cannot be found",
				httpServletRequest.getMethod(), httpServletRequest.getRequestURI()));
	}

}
