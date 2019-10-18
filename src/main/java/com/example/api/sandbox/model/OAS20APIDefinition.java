package com.example.api.sandbox.model;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.jena.ext.com.google.common.collect.ImmutableMap;
import org.dizitart.no2.Nitrite;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.api.sandbox.exception.DefinitionNotFoundException;
import com.example.api.sandbox.exception.InvalidInputException;
import com.example.api.sandbox.exception.RequestNotFoundException;
import com.example.api.sandbox.utils.SwaggerPathUtils;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import lombok.Getter;
import lombok.extern.flogger.Flogger;

/**
 * 
 * 
 * @since v1
 */
@Flogger
public class OAS20APIDefinition extends AbstractAPIDefinition {

	@Autowired
	private Nitrite database;

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
	public Object matchRequest(final HttpServletRequest httpServletRequest) throws RequestNotFoundException {
		if (swagger.getPaths().isEmpty()) {
			throw new DefinitionNotFoundException();
		}

		for (Entry<String, Path> entry : swagger.getPaths().entrySet()) {
			if (SwaggerPathUtils.pathToRegex(entry.getValue(), entry.getKey())
					.matcher(httpServletRequest.getRequestURI()).matches()) {
				final HttpMethod httpMethod = HttpMethod.valueOf(httpServletRequest.getMethod());
				if (entry.getValue().getOperationMap().containsKey(httpMethod)) {
					log.atInfo().log("Request matched to path [%s] %s", httpMethod.name(), entry.getKey());

					// Get Operation
					Operation operation = entry.getValue().getOperationMap().get(httpMethod);

					validateInput(httpServletRequest, operation);

					// Determine response

					return ImmutableMap.of("message", "successful");
				}
			}
		}

		throw new RequestNotFoundException(String.format("The request [%s] %s cannot be found",
				httpServletRequest.getMethod(), httpServletRequest.getRequestURI()));
	}

	/**
	 * Validates that the input is correct
	 * 
	 * @param httpServletRequest
	 * @param operation
	 */
	private void validateInput(final HttpServletRequest httpServletRequest, Operation operation) {
		// generate list of parameters to validate
		List<String> parametersToValidate = new ArrayList<>();
		operation.getParameters().forEach(p -> parametersToValidate.add(p.getName()));

		for (Parameter parameter : operation.getParameters()) {
			switch (parameter.getIn()) {
			case "body":
				parametersToValidate.remove(parameter.getName());
				break;
			case "header":
				Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
				while(headerNames.hasMoreElements()) {
					final String headerName = headerNames.nextElement();
					if (headerName.equals(parameter.getName())) {
						parametersToValidate.remove(parameter.getName());
					}
				}
				break;
			case "path":
				parametersToValidate.remove(parameter.getName());
				break;
			default: // formData
				Enumeration<String> parameterNames = httpServletRequest.getParameterNames();
				while (parameterNames.hasMoreElements()) {
					final String parameterName = parameterNames.nextElement();
					if (parameterName.equals(parameter.getName())) {
						parametersToValidate.remove(parameter.getName());
					}
				}
			}
		}
		if (parametersToValidate.size() > 0) {
			throw new InvalidInputException(405, "The request is invalid!", parametersToValidate);
		}
	}

}
