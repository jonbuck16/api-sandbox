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

import com.example.api.sandbox.exception.InvalidInputException;
import com.example.api.sandbox.exception.PathNotFoundException;
import com.example.api.sandbox.exception.RequestNotFoundException;
import com.example.api.sandbox.utils.OpenApiPathUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.parameters.Parameter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.flogger.Flogger;

/**
 * Processes an OpenAPISpecification
 * 
 * @since v1
 */
@Flogger
public class OAS30APIDefinition extends AbstractAPIDefinition {

    @Autowired
    private Nitrite database;

    @Setter
    @Getter
    private OpenAPI openApi;

    public OAS30APIDefinition() {
        super(ModelType.OAS3);
    }

    /**
     * 
     * @param operation
     * @param httpServletRequest
     * @return
     */
    private CompletableFuture<RequestResponse> processDel(Operation operation, HttpServletRequest httpServletRequest) {
        return CompletableFuture.completedFuture(RequestResponse.builder().httpStatus(HttpStatus.NOT_IMPLEMENTED).build());
    }

    /**
     * Processes the incoming get request
     * 
     * @param operation
     * @param httpServletRequest
     */
    @SuppressWarnings("rawtypes")
    private CompletableFuture<RequestResponse> processGet(final String path, Operation operation, HttpServletRequest httpServletRequest) {
        ObjectRepository<Map> repository = database.getRepository(Map.class);
        Cursor<Map> results = null;
        if (operation.getParameters().isEmpty()) {
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
     * @param operation
     * @param httpServletRequest
     * @return
     */
    private CompletableFuture<RequestResponse> processHed(Operation operation, HttpServletRequest httpServletRequest) {
        return CompletableFuture.completedFuture(RequestResponse.builder().httpStatus(HttpStatus.NOT_IMPLEMENTED).build());
    }

    /**
     * 
     * @param operation
     * @param httpServletRequest
     * @return
     */
    private CompletableFuture<RequestResponse> processOpt(Operation operation, HttpServletRequest httpServletRequest) {
        return CompletableFuture.completedFuture(RequestResponse.builder().httpStatus(HttpStatus.NOT_IMPLEMENTED).build());
    }

    /**
     * 
     * @param operation
     * @param httpServletRequest
     * @return
     */
    private CompletableFuture<RequestResponse> processPch(Operation operation, HttpServletRequest httpServletRequest) {
        return CompletableFuture.completedFuture(RequestResponse.builder().httpStatus(HttpStatus.NOT_IMPLEMENTED).build());
    }

    /**
     * 
     * @param operation
     * @param httpServletRequest
     * @return
     */
    @SuppressWarnings("rawtypes")
    private CompletableFuture<RequestResponse> processPst(Operation operation, HttpServletRequest httpServletRequest) {
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
     * @param operation
     * @param httpServletRequest
     * @return
     */
    private CompletableFuture<RequestResponse> processPut(Operation operation, HttpServletRequest httpServletRequest) {
        return CompletableFuture.completedFuture(RequestResponse.builder().httpStatus(HttpStatus.NOT_IMPLEMENTED).build());
    }

    @Override
    public CompletableFuture<RequestResponse> processRequest(HttpServletRequest httpServletRequest)
            throws RequestNotFoundException {
        if (openApi.getPaths().isEmpty()) {
            throw new PathNotFoundException();
        }

        for (Entry<String, PathItem> entry : openApi.getPaths().entrySet()) {
            final HttpMethod httpMethod = HttpMethod.valueOf(httpServletRequest.getMethod());
            if (OpenApiPathUtils.pathToRegex(entry.getKey(), entry.getValue(), httpMethod)
                    .matcher(httpServletRequest.getRequestURI()).matches()) {
                if (entry.getValue().readOperationsMap().containsKey(httpMethod)) {
                    log.atInfo().log("Request matched to path [%s] %s", httpMethod.name(), entry.getKey());
                    Operation operation = entry.getValue().readOperationsMap().get(httpMethod);
                    validateOperation(operation, httpServletRequest);
                    switch (httpMethod) {
                    case PATCH:
                        return processPch(operation, httpServletRequest);
                    case POST:
                        return processPst(operation, httpServletRequest);
                    case PUT:
                        return processPut(operation, httpServletRequest);
                    case TRACE:
                        return processTce(operation, httpServletRequest);
                    case DELETE:
                        return processDel(operation, httpServletRequest);
                    case OPTIONS:
                        return processOpt(operation, httpServletRequest);
                    case HEAD:
                        return processHed(operation, httpServletRequest);
                    default:
                        return processGet(entry.getKey(), operation, httpServletRequest);
                    }
                }
            }
        }
        throw new RequestNotFoundException(String.format("The request [%s] %s cannot be found", httpServletRequest.getMethod(),
                httpServletRequest.getRequestURI()));
    }

    /**
     * 
     * @param operation
     * @param httpServletRequest
     * @return
     */
    private CompletableFuture<RequestResponse> processTce(Operation operation, HttpServletRequest httpServletRequest) {
        return CompletableFuture.completedFuture(RequestResponse.builder().httpStatus(HttpStatus.NOT_IMPLEMENTED).build());
    }

    /**
     * Validates that the request contains the required elements from the operation
     * 
     * @param operation          the Operation to validate the request against
     * @param httpServletRequest the incoming request to validate
     * @throws an InvalidInputException if the request does not match the operation.
     */
    private void validateOperation(final Operation operation, final HttpServletRequest httpServletRequest) {
        // Generate list of parameters to validate against, we will use this list to
        // 'tick off' which parameters exist in the request and therefore if anything is
        // left then that is missing
        if (operation.getParameters() != null) {
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
                throw new InvalidInputException(400, "The request is invalid!", parametersToValidate);
            }
        }

        if (operation.getRequestBody() != null) {
            if (httpServletRequest.getContentLength() == 0) {
                throw new InvalidInputException(400, "The request is invalid!", ImmutableList.of("Request Body"));
            }
        }
    }
    
    /**
     * Retrieves the value for the request for the specified value.
     * 
     * @param operation          the operation involved
     * @param parameter          the parameter object from the API definition
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
        switch (parameter.getSchema().getType()) {
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
    
}
