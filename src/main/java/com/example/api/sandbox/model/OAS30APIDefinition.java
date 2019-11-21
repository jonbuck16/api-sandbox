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

import com.example.api.sandbox.exception.EndpointNotFoundException;
import com.example.api.sandbox.exception.InternalServerException;
import com.example.api.sandbox.exception.InvalidInputException;
import com.example.api.sandbox.exception.PathNotFoundException;
import com.example.api.sandbox.utils.OpenApiUtils;
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
     * Retrieves the value for the request for the specified value.
     * 
     * @param parameter          the parameter object from the API definition
     * @param operation          the operation involved
     * @param httpServletRequest in incoming request from which to try and get data
     * 
     * @return an ObjectFilter
     */
    private ObjectFilter constructObjectFilter(final String path, final Parameter parameter, final Operation operation,
            final HttpServletRequest httpServletRequest) {
        switch (parameter.getIn()) {
        case "query":
            final String[] parameterValues = httpServletRequest.getParameterValues(parameter.getName());
            List<ObjectFilter> filters = new LinkedList<>();
            Arrays.asList(parameterValues).forEach(s -> filters.add(ObjectFilters.eq(parameter.getName(), s)));
            return ObjectFilters.or(filters.toArray(new ObjectFilter[filters.size()]));
        case "header":
            return ObjectFilters.eq(parameter.getName(), httpServletRequest.getHeader(parameter.getName()));
        default: // path
            return handlePathValue(path, parameter, httpServletRequest);
        }
    }

    /**
     * Extracts a path variable value from the incoming request.
     * 
     * @param path
     * @param parameter
     * @param httpServletRequest
     * @return
     */
    private ObjectFilter handlePathValue(final String path, final Parameter parameter,
            final HttpServletRequest httpServletRequest) {
        List<String> pathParts = Arrays.asList(path.split("/"));
        List<String> requestParts = Arrays.asList(httpServletRequest.getRequestURI().split("/"));
        switch (parameter.getSchema().getType()) {
        case "array":

            break;
        case "boolean":

            break;
        case "integer":

            break;
        default:
            return ObjectFilters.eq(parameter.getName(),
                    requestParts.get(pathParts.indexOf(String.format("{%s}", parameter.getName()))));
        }
        return null;

    }

    /**
     * 
     * @param operation
     * @param httpServletRequest
     * @return
     */
    private CompletableFuture<RequestResponse> processDel(final String path, final Operation operation,
            final HttpServletRequest httpServletRequest) {

        ObjectRepository<Map> repository = database.getRepository(Map.class);
        List<ObjectFilter> filters = new LinkedList<>();
        if (operation.getParameters() != null) {
            operation.getParameters()
                    .forEach(parameter -> filters.add(constructObjectFilter(path, parameter, operation, httpServletRequest)));
        }

        if (repository.remove(ObjectFilters.and(filters.toArray(new ObjectFilter[filters.size()]))).getAffectedCount() > 0) {
            return CompletableFuture.completedFuture(RequestResponse.builder().httpStatus(HttpStatus.OK).build());
        }
        return CompletableFuture.completedFuture(RequestResponse.builder().httpStatus(HttpStatus.NOT_FOUND).build());

    }

    /**
     * Processes the incoming get request
     * 
     * @param operation
     * @param httpServletRequest
     */
    @SuppressWarnings("rawtypes")
    private CompletableFuture<RequestResponse> processGet(final String path, Operation operation,
            HttpServletRequest httpServletRequest) {
        Cursor<Map> results = retrieveResults(path, operation, httpServletRequest);
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
    private CompletableFuture<RequestResponse> processHed(final String path, final Operation operation,
            final HttpServletRequest httpServletRequest) {
        return CompletableFuture.completedFuture(RequestResponse.builder().httpStatus(HttpStatus.NOT_IMPLEMENTED).build());
    }

    /**
     * 
     * @param operation
     * @param httpServletRequest
     * @return
     */
    private CompletableFuture<RequestResponse> processOpt(final String path, final Operation operation,
            final HttpServletRequest httpServletRequest) {
        return CompletableFuture.completedFuture(RequestResponse.builder().httpStatus(HttpStatus.NOT_IMPLEMENTED).build());
    }

    /**
     * 
     * @param operation
     * @param httpServletRequest
     * @return
     */
    private CompletableFuture<RequestResponse> processPch(final String path, final Operation operation,
            final HttpServletRequest httpServletRequest) {
        return CompletableFuture.completedFuture(RequestResponse.builder().httpStatus(HttpStatus.NOT_IMPLEMENTED).build());
    }

    /**
     * Post and Put should be treated the same..
     * 
     * @param path
     * @param operation
     * @param httpServletRequest
     * @return
     */
    @SuppressWarnings("rawtypes")
    private CompletableFuture<RequestResponse> processPostPut(String path, Operation operation,
            HttpServletRequest httpServletRequest) {
        try (InputStreamReader inputStreamReader = new InputStreamReader(httpServletRequest.getInputStream(),
                StandardCharsets.UTF_8)) {

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> data = mapper.readValue(inputStreamReader, new TypeReference<Map<String, Object>>() {
            });

            List<ObjectFilter> filters = new LinkedList<>();
            if (operation.getParameters() != null) {
                operation.getParameters()
                        .forEach(parameter -> filters.add(constructObjectFilter(path, parameter, operation, httpServletRequest)));
            }

            ObjectRepository<Map> repository = database.getRepository(Map.class);
            Cursor<Map> results = repository.find(ObjectFilters.and(filters.toArray(new ObjectFilter[filters.size()])));
            switch (results.size()) {
            case 0:
                if (repository.insert(data).getAffectedCount() > 0) {
                    return CompletableFuture
                            .completedFuture(RequestResponse.builder().data(data).httpStatus(HttpStatus.CREATED).build());
                } else {
                    return CompletableFuture
                            .completedFuture(RequestResponse.builder().httpStatus(HttpStatus.INTERNAL_SERVER_ERROR).build());
                }
            case 1:
                if (repository.update(ObjectFilters.and(filters.toArray(new ObjectFilter[filters.size()])), data)
                        .getAffectedCount() > 0) {
                    return CompletableFuture
                            .completedFuture(RequestResponse.builder().data(results).httpStatus(HttpStatus.CREATED).build());
                } else {
                    return CompletableFuture
                            .completedFuture(RequestResponse.builder().httpStatus(HttpStatus.INTERNAL_SERVER_ERROR).build());
                }
            default:
                return CompletableFuture
                        .completedFuture(RequestResponse.builder().httpStatus(HttpStatus.UNPROCESSABLE_ENTITY).build());
            }

        } catch (IOException e) {
            log.atSevere().log(e.getMessage());
            throw new InternalServerException("Too many results!");
        }
    }

    /**
     * 
     * @param operation
     * @param httpServletRequest
     * @return
     */
    private CompletableFuture<RequestResponse> processPst(final String path, Operation operation,
            HttpServletRequest httpServletRequest) {
        return this.processPostPut(path, operation, httpServletRequest);
    }

    /**
     * 
     * @param operation
     * @param httpServletRequest
     * @return
     */
    private CompletableFuture<RequestResponse> processPut(final String path, final Operation operation,
            final HttpServletRequest httpServletRequest) {
        return this.processPostPut(path, operation, httpServletRequest);
    }

    @Override
    public CompletableFuture<RequestResponse> processRequest(HttpServletRequest httpServletRequest)
            throws EndpointNotFoundException {
        if (openApi.getPaths().isEmpty()) {
            throw new PathNotFoundException();
        }

        for (Entry<String, PathItem> entry : openApi.getPaths().entrySet()) {
            final HttpMethod httpMethod = HttpMethod.valueOf(httpServletRequest.getMethod());
            if (OpenApiUtils.pathToRegex(entry.getKey(), entry.getValue(), httpMethod).matcher(httpServletRequest.getRequestURI())
                    .matches()) {
                if (entry.getValue().readOperationsMap().containsKey(httpMethod)) {
                    log.atInfo().log("Request matched to path [%s] %s", httpMethod.name(), entry.getKey());
                    Operation operation = entry.getValue().readOperationsMap().get(httpMethod);
                    validateOperation(operation, httpServletRequest);
                    switch (httpMethod) {
                    case PATCH:
                        return processPch(entry.getKey(), operation, httpServletRequest);
                    case POST:
                        return processPst(entry.getKey(), operation, httpServletRequest);
                    case PUT:
                        return processPut(entry.getKey(), operation, httpServletRequest);
                    case TRACE:
                        return processTce(entry.getKey(), operation, httpServletRequest);
                    case DELETE:
                        return processDel(entry.getKey(), operation, httpServletRequest);
                    case OPTIONS:
                        return processOpt(entry.getKey(), operation, httpServletRequest);
                    case HEAD:
                        return processHed(entry.getKey(), operation, httpServletRequest);
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
     * 
     * @param operation
     * @param httpServletRequest
     * @return
     */
    private CompletableFuture<RequestResponse> processTce(final String path, final Operation operation,
            final HttpServletRequest httpServletRequest) {
        return CompletableFuture.completedFuture(RequestResponse.builder().httpStatus(HttpStatus.NOT_IMPLEMENTED).build());
    }

    /**
     * 
     * @param path
     * @param operation
     * @param httpServletRequest
     * @return
     */
    @SuppressWarnings("rawtypes")
    private Cursor<Map> retrieveResults(final String path, Operation operation, HttpServletRequest httpServletRequest) {
        Cursor<Map> results = null;
        ObjectRepository<Map> repository = database.getRepository(Map.class);
        if (operation.getParameters() == null || operation.getParameters().isEmpty()) {
            results = repository.find();
        } else {
            List<ObjectFilter> filters = new LinkedList<>();
            operation.getParameters()
                    .forEach(parameter -> filters.add(constructObjectFilter(path, parameter, operation, httpServletRequest)));
            ObjectFilter objectFilter = ObjectFilters.and(filters.toArray(new ObjectFilter[filters.size()]));
            results = repository.find(objectFilter);
        }
        return results;
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
                    case "cookie": // TODO
                        break;
                    case "query":
                        Enumeration<String> parameterNames = httpServletRequest.getParameterNames();
                        while (parameterNames.hasMoreElements()) {
                            final String parameterName = parameterNames.nextElement();
                            if (parameterName.equals(parameter.getName())) {
                                parametersToValidate.remove(parameter.getName());
                            }
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
                    default: // TODO as just removing is not good enough...
                        parametersToValidate.remove(parameter.getName());
                    }
                } else {
                    parametersToValidate.remove(parameter.getName());
                }
            }
            if (parametersToValidate.size() > 0) {
                throw new InvalidInputException(400, "The request is invalid!", parametersToValidate);
            }
        }

        if (operation.getRequestBody() != null && httpServletRequest.getContentLength() == 0) {
            throw new InvalidInputException(400, "The request is invalid!", ImmutableList.of("Request Body"));
        }
    }

}
