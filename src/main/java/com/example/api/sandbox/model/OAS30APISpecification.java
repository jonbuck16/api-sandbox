package com.example.api.sandbox.model;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
import com.example.api.sandbox.exception.PathNotFoundException;
import com.example.api.sandbox.utils.OpenApiPathUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.flogger.Flogger;

/**
 * Processes an OpenAPI version 3.x Specification
 * 
 * @since v1
 */
@Flogger
public class OAS30APISpecification extends AbstractAPISpecification {

    @Autowired
    private Nitrite database;

    @Setter
    @Getter
    private OpenAPI openApi;

    public OAS30APISpecification() {
        super(ModelType.OAS3);
    }

    /**
     * Processes a DELETE request
     * 
     * @param path               the path of the request
     * @param operation          the operation that is being performed
     * @param httpServletRequest the actual request to process
     * @return
     */
    @SuppressWarnings("rawtypes")
    private CompletableFuture<RequestResponse> processDel(final String path, final Operation operation,
            final HttpServletRequest httpServletRequest) {
        ObjectRepository<Map> repository = database.getRepository(Map.class);

        List<ObjectFilter> filters = new LinkedList<>();
        if (operation.getParameters() != null) {
            operation.getParameters().stream().map(parameter -> OpenApiPathUtils.constructObjectFilter(path, parameter.getIn(),
                    parameter.getName(), parameter.getSchema().getType(), httpServletRequest)).forEach(filters::add);
        }

        if (repository.remove(ObjectFilters.and(filters.toArray(new ObjectFilter[filters.size()]))).getAffectedCount() > 0) {
            return CompletableFuture.completedFuture(RequestResponse.builder().httpStatus(HttpStatus.OK).build());
        }
        return CompletableFuture.completedFuture(RequestResponse.builder().httpStatus(HttpStatus.NOT_FOUND).build());

    }

    /**
     * Processes a GET request
     * 
     * @param path
     * @param operation
     * @param httpServletRequest
     * @return
     */
    @SuppressWarnings("rawtypes")
    private CompletableFuture<RequestResponse> processGet(final String path, Operation operation,
            HttpServletRequest httpServletRequest) {
        Cursor<Map> results = null;
        ObjectRepository<Map> repository = database.getRepository(Map.class);
        if (operation.getParameters() == null || operation.getParameters().isEmpty()) {
            results = repository.find();
        } else {
            List<ObjectFilter> filters = new LinkedList<>();
            operation.getParameters().stream().map(parameter -> OpenApiPathUtils.constructObjectFilter(path, parameter.getIn(),
                    parameter.getName(), parameter.getSchema().getType(), httpServletRequest)).forEach(filters::add);
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
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private CompletableFuture<RequestResponse> processPostPut(String path, Operation operation,
            HttpServletRequest httpServletRequest) {
        try (InputStreamReader inputStreamReader = new InputStreamReader(httpServletRequest.getInputStream(),
                StandardCharsets.UTF_8)) {

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> data = mapper.readValue(inputStreamReader, Map.class);

            List<ObjectFilter> filters = new LinkedList<>();
            if (operation.getParameters() != null) {
                operation.getParameters().stream().map(parameter -> OpenApiPathUtils.constructObjectFilter(path, parameter.getIn(),
                        parameter.getName(), parameter.getSchema().getType(), httpServletRequest)).forEach(filters::add);
                ;
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
            if (OpenApiPathUtils.pathToRegex(entry.getKey(), entry.getValue(), httpMethod)
                    .matcher(httpServletRequest.getRequestURI()).matches()) {
                if (entry.getValue().readOperationsMap().containsKey(httpMethod)) {
                    log.atInfo().log("Request matched to path [%s] %s", httpMethod.name(), entry.getKey());
                    Operation operation = entry.getValue().readOperationsMap().get(httpMethod);
                    OpenApiPathUtils.validateOperation(operation, httpServletRequest);
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

}
