package com.example.api.sandbox.model;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectFilter;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.example.api.sandbox.exception.PathNotFoundException;
import com.example.api.sandbox.exception.InvalidInputException;
import com.example.api.sandbox.exception.EndpointNotFoundException;
import com.example.api.sandbox.utils.SwaggerPathUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.flogger.Flogger;

/**
 * 
 * 
 * @since v1
 */
@Flogger
public class OAS20APISpecification extends AbstractAPISpecification {

    @Autowired
    private Nitrite database;

    @Getter
    @Setter
    private Swagger swagger;

    public OAS20APISpecification() {
        super(ModelType.OAS2);
    }

    /**
     * Retrieves the value for the request for the specified value.
     * 
     * @param operation          the operation involved
     * @param parameter          the parameter object from the API Specification
     * @param httpServletRequest in incoming request from which to try and get data
     * @return an ObjectFilter
     */
    private ObjectFilter constructObjectFilter(final String path, final Operation operation, final Parameter parameter,
            final HttpServletRequest httpServletRequest) {
        Object value = null;
        switch (parameter.getIn()) {
        case "query":
            value = httpServletRequest.getParameter(parameter.getName());
            break;
        case "header":
            value = httpServletRequest.getHeader(parameter.getName());
            break;
        default: // path
            value = handlePathValue(path, parameter, httpServletRequest);
        }
        return ObjectFilters.eq(parameter.getName(), value);
    }

    /**
     * Extracts a path variable value from the incoming request.
     * 
     * @param path
     * @param parameter
     * @param httpServletRequest
     * @return
     */
    private Object handlePathValue(final String path, final Parameter parameter, final HttpServletRequest httpServletRequest) {
        Object value;
        List<String> pathParts = Arrays.asList(path.split("/"));
        List<String> requestParts = Arrays.asList(httpServletRequest.getRequestURI().split("/"));
        PathParameter pathParameter = (PathParameter) parameter;
        switch (pathParameter.getType()) {
        case "boolean":
            value = Boolean.class.cast(requestParts.get(pathParts.indexOf(String.format("{%s}", parameter.getName()))));
        case "integer":
            final int indexValue = pathParts.indexOf(String.format("{%s}", parameter.getName()));
            final String rawValue = requestParts.get(indexValue);
            value = Integer.valueOf(rawValue);
            break;
        default:
            value = requestParts.get(pathParts.indexOf(String.format("{%s}", parameter.getName())));
        }
        return value;
    }

    /**
     * Retrieve data from the in memory database based on the API operation and
     * incoming request.
     * 
     * @param path               the path value
     * @param operation          the operation
     * @param httpServletRequest the incoming HTTP request
     */
    @SuppressWarnings("rawtypes")
    private CompletableFuture<RequestResponse> processGet(final String path, Operation operation,
            HttpServletRequest httpServletRequest) {
        ObjectRepository<Map> repository = database.getRepository(Map.class);
        Cursor<Map> results = null;
        if (operation.getParameters() == null || operation.getParameters().isEmpty()) {
            results = repository.find();
        } else {
            List<ObjectFilter> filters = new LinkedList<>();
            operation.getParameters()
                    .forEach(parameter -> filters.add(constructObjectFilter(path, operation, parameter, httpServletRequest)));
            results = repository.find(ObjectFilters.and(filters.toArray(new ObjectFilter[filters.size()])));
        }
        if (results != null && results.size() > 0) {
            return CompletableFuture.completedFuture(RequestResponse.builder().data(results).httpStatus(HttpStatus.OK).build());
        } else {
            return CompletableFuture.completedFuture(RequestResponse.builder().httpStatus(HttpStatus.NOT_FOUND).build());
        }
    }

    /**
     * 
     * @param httpServletRequest
     * @param operation
     */
    @SuppressWarnings("rawtypes")
    private CompletableFuture<RequestResponse> processPst(HttpServletRequest httpServletRequest, Operation operation) {
        try (InputStreamReader inputStreamReader = new InputStreamReader(httpServletRequest.getInputStream(),
                StandardCharsets.UTF_8)) {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> data = mapper.readValue(inputStreamReader, new TypeReference<Map<String, Object>>() {});
            ObjectRepository<Map> repository = database.getRepository(Map.class);
            repository.insert(data);
            return CompletableFuture.completedFuture(RequestResponse.builder().data(data).httpStatus(HttpStatus.CREATED).build());
        } catch (IOException e) {
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
    private CompletableFuture<RequestResponse> processPut(HttpServletRequest httpServletRequest, Operation operation) {
        ObjectRepository<Map> repository = database.getRepository(Map.class);
        Cursor<Map> results = repository.find(ObjectFilters.and(ObjectFilters.eq("id", 0)));
        if (results.hasMore()) {
            return CompletableFuture
                    .completedFuture(RequestResponse.builder().data(results).httpStatus(HttpStatus.CREATED).build());
        }
        return CompletableFuture.completedFuture(RequestResponse.EMPTY);
    }

    /**
     * 
     */
    @Override
    public CompletableFuture<RequestResponse> processRequest(final HttpServletRequest httpServletRequest)
            throws EndpointNotFoundException {
        if (swagger.getPaths().isEmpty()) {
            throw new PathNotFoundException();
        }

        for (Entry<String, Path> entry : swagger.getPaths().entrySet()) {
            final HttpMethod httpMethod = HttpMethod.valueOf(httpServletRequest.getMethod());
            if (SwaggerPathUtils.pathToRegex(entry.getKey(), entry.getValue(), httpMethod).matcher(httpServletRequest.getRequestURI())
                    .matches()) {
                if (entry.getValue().getOperationMap().containsKey(httpMethod)) {
                    log.atInfo().log("Request matched to path [%s] %s", httpMethod.name(), entry.getKey());
                    Operation operation = entry.getValue().getOperationMap().get(httpMethod);
                    validateOperation(operation, httpServletRequest);
                    switch (httpMethod) {
                    case PUT:
                        return processPut(httpServletRequest, operation);
                    case POST:
                        return processPst(httpServletRequest, operation);
                    case PATCH:
                        break;
                    case OPTIONS:
                        break;
                    case HEAD:
                        break;
                    default:
                        return processGet(entry.getKey(), operation, httpServletRequest);
                    }
                }
            }
        }

        throw new EndpointNotFoundException(String.format("The endpoint [%s] %s cannot be found", httpServletRequest.getMethod(),
                httpServletRequest.getRequestURI()));
    }

    /**
     * Validates that the input is correct
     * 
     * @param httpServletRequest
     * @param operation
     */
    private void validateOperation(final Operation operation, final HttpServletRequest httpServletRequest) {
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
                default: // Query
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
            throw new InvalidInputException(400, "The request is invalid!", parametersToValidate);
        }
    }

}
