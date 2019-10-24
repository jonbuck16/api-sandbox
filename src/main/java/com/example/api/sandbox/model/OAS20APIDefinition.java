package com.example.api.sandbox.model;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.example.api.sandbox.exception.DefinitionNotFoundException;
import com.example.api.sandbox.exception.InvalidInputException;
import com.example.api.sandbox.exception.RequestNotFoundException;
import com.example.api.sandbox.utils.SwaggerPathUtils;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import lombok.Getter;
import lombok.Setter;
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
	@Setter
	private Swagger swagger;

	public OAS20APIDefinition() {
		super(ModelType.OAS2);
	}

	/**
	 * 
	 */
	@Override
	public CompletableFuture<RequestResponse> processRequest(final HttpServletRequest httpServletRequest)
			throws RequestNotFoundException {
		if (swagger.getPaths().isEmpty()) {
			throw new DefinitionNotFoundException();
		}

		for (Entry<String, Path> entry : swagger.getPaths().entrySet()) {
			if (SwaggerPathUtils.pathToRegex(entry.getValue(), entry.getKey())
					.matcher(httpServletRequest.getRequestURI()).matches()) {
				final HttpMethod httpMethod = HttpMethod.valueOf(httpServletRequest.getMethod());
				if (entry.getValue().getOperationMap().containsKey(httpMethod)) {
					log.atInfo().log("Request matched to path [%s] %s", httpMethod.name(), entry.getKey());

					// Get Operation from the definition
					Operation operation = entry.getValue().getOperationMap().get(httpMethod);

					// Validate that the data supplied to the application is valid for the request
					validateInput(httpServletRequest, operation);

					// Generate the response data, this will either be generated from the request
					// and persisted in the internal database, an entry updated from the request and
					// persisted, or just simply retrieved from the database...
					if (httpMethod.equals(HttpMethod.POST)) {
						log.atInfo().log("Persisting data...");
						return createResponse(httpServletRequest, operation);
					} else if (httpMethod.equals(HttpMethod.PUT)) {
						log.atInfo().log("Updating data...");
						return updateResponse(httpServletRequest, operation);
					} else {
						log.atInfo().log("Retrieving data....");
						return retrieveResponse(httpServletRequest, operation);
					}

				}
			}
		}

		throw new RequestNotFoundException(String.format("The request [%s] %s cannot be found",
				httpServletRequest.getMethod(), httpServletRequest.getRequestURI()));
	}

	/**
	 * 
	 * @param httpServletRequest
	 * @param operation
	 */
	@SuppressWarnings("rawtypes")
	private CompletableFuture<RequestResponse> createResponse(HttpServletRequest httpServletRequest,
			Operation operation) {
		try {
			Map<?, ?> data = new Gson().fromJson(
					new InputStreamReader(httpServletRequest.getInputStream(), StandardCharsets.UTF_8), Map.class);
			ObjectRepository<Map> repository = database.getRepository(Map.class);
			repository.insert(data);
			return CompletableFuture
					.completedFuture(RequestResponse.builder().data(data).httpStatus(HttpStatus.CREATED).build());
		} catch (JsonSyntaxException | JsonIOException | IOException e) {
			log.atSevere().log(e.getMessage());
		}
		return CompletableFuture.completedFuture(RequestResponse.EMPTY);
	}

	/**
	 * 
	 * @param httpServletRequest
	 * @param operation
	 */
	@SuppressWarnings("rawtypes")
	private CompletableFuture<RequestResponse> updateResponse(HttpServletRequest httpServletRequest, Operation operation) {
		try {
			
			ObjectRepository<Map> repository = database.getRepository(Map.class);

			Cursor<Map> results = repository.find(ObjectFilters.and(ObjectFilters.eq("id", 0)));
			
			return CompletableFuture
					.completedFuture(RequestResponse.builder().data(results).httpStatus(HttpStatus.CREATED).build());
		
		} catch (JsonSyntaxException | JsonIOException e) {
			log.atSevere().log(e.getMessage());
		}
		return CompletableFuture.completedFuture(RequestResponse.EMPTY);
	}

	/**
	 * 
	 * @param httpServletRequest
	 * @param operation
	 */
	private CompletableFuture<RequestResponse> retrieveResponse(HttpServletRequest httpServletRequest, Operation operation) {
		try {
			
			ObjectRepository<Map> repository = database.getRepository(Map.class);

			Cursor<Map> results = repository.find(ObjectFilters.and(ObjectFilters.eq("status", "available")));
			
			return CompletableFuture
					.completedFuture(RequestResponse.builder().data(results).httpStatus(HttpStatus.CREATED).build());
		
		} catch (JsonSyntaxException | JsonIOException e) {
			log.atSevere().log(e.getMessage());
		}
		return CompletableFuture.completedFuture(RequestResponse.EMPTY);
	}

	/**
	 * Validates that the input is correct
	 * 
	 * @param httpServletRequest
	 * @param operation
	 */
	private void validateInput(final HttpServletRequest httpServletRequest, final Operation operation) {
		// Generate list of parameters to validate against, we will use this list to
		// 'tick off' which parameters exist in the request and therefore if anything is
		// left then that is missing
		final List<String> parametersToValidate = new ArrayList<>();
		operation.getParameters().forEach(p -> parametersToValidate.add(p.getName()));

		for (Parameter parameter : operation.getParameters()) {
			if (parameter.getRequired()) {
				switch (parameter.getIn()) {
				case "body":
					// TODO Validate body against the model
					if (httpServletRequest.getContentLength() > 0) {
						parametersToValidate.remove(parameter.getName());
					}
					break;
				case "header":
					Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
					while (headerNames.hasMoreElements()) {
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
			} else {
				parametersToValidate.remove(parameter.getName());
			}
		}
		if (parametersToValidate.size() > 0) {
			throw new InvalidInputException(405, "The request is invalid!", parametersToValidate);
		}
	}

}
