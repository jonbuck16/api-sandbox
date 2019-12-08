package com.example.api.sandbox.model;

import com.example.api.sandbox.Constants;
import com.example.api.sandbox.HttpStatusPatterns;
import com.example.api.sandbox.OAS3MediaTypes;
import com.example.api.sandbox.exception.EndpointNotFoundException;
import com.example.api.sandbox.exception.InternalServerException;
import com.example.api.sandbox.exception.PathNotFoundException;
import com.example.api.sandbox.service.ContentService;
import com.example.api.sandbox.utils.OpenApiPathUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.flogger.Flogger;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectFilter;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

/**
 * Processes an OpenAPI version 3.x Specification
 * <p>
 * OpenAPI Specification (formerly Swagger Specification) is an API description format for REST APIs. An OpenAPI file
 * allows you to describe your entire API, including:
 * <ul>
 *     <li>Available endpoints (/users) and operations on each endpoint (GET /users, POST /users)</li>
 *     <li>Operation parameters Input and output for each operation</li>
 *     <li>Authentication methods</li>
 *     <li>Contact information, license, terms of use and other information.</li>
 * </ul>
 * </p>
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
     * Processes a DELETE request.
     *
     * @param path               the path of the request
     * @param operation          the operation that is being performed
     * @param httpServletRequest the incoming request to process
     * @return the response from the DELETE request
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

        if (repository.remove(ObjectFilters.and(filters.toArray(new ObjectFilter[0]))).getAffectedCount() > 0) {
            return CompletableFuture.completedFuture(RequestResponse.builder().httpStatus(HttpStatus.OK).build());
        }
        return CompletableFuture.completedFuture(RequestResponse.builder().httpStatus(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Processes a GET request.
     *
     * @param path               the path of the request
     * @param operation          the operation details from the API specification
     * @param httpServletRequest the incoming request to process
     * @return the response from the GET request
     */
    @SuppressWarnings("rawtypes")
    private CompletableFuture<RequestResponse> processGet(final String path, Operation operation,
                                                          HttpServletRequest httpServletRequest) {
        Cursor<Map> results;
        final ObjectRepository<Map> repository = database.getRepository(Map.class);
        if (operation.getParameters() == null || operation.getParameters().isEmpty()) {
            results = repository.find();
        } else {
            List<ObjectFilter> filters = new LinkedList<>();
            operation.getParameters().stream().map(parameter -> OpenApiPathUtils.constructObjectFilter(path, parameter.getIn(),
                    parameter.getName(), parameter.getSchema().getType(), httpServletRequest)).forEach(filters::add);
            results = repository.find(ObjectFilters.and(filters.toArray(new ObjectFilter[0])));
        }

        final String accept = httpServletRequest.getHeader(HttpHeaders.ACCEPT);
        final MediaType mediaType = getMediaType(operation, accept);
        if (results != null && results.size() > 0) {
            if (mediaType != null && mediaType.getSchema().getType() != null && mediaType.getSchema().getType().equals(Constants.ARRAY)) {
                if ("application/xml".equalsIgnoreCase(accept) && mediaType.getSchema().getXml() != null) {
                    return CompletableFuture.completedFuture(RequestResponse.builder().data(ContentService.asType(accept,
                            mediaType.getSchema().getXml().getName(),
                            results.toList())).httpStatus(HttpStatus.OK).build());
                } else {
                    return CompletableFuture.completedFuture(RequestResponse.builder().data(ContentService.asType(accept,
                            results.toList())).httpStatus(HttpStatus.OK).build());
                }
            } else {
                return CompletableFuture.completedFuture(RequestResponse.builder().data(ContentService.asType(accept,
                        results.firstOrDefault())).httpStatus(HttpStatus.OK).build());
            }
        } else {
            if (mediaType != null && mediaType.getSchema().getType() != null && mediaType.getSchema().getType().equals(Constants.ARRAY)) {
                if ("application/xml".equalsIgnoreCase(accept) && mediaType.getSchema().getXml() != null) {
                    return CompletableFuture.completedFuture(RequestResponse.builder().data(ContentService.asType(accept,
                            mediaType.getSchema().getXml().getName(),
                            ImmutableList.of())).httpStatus(HttpStatus.OK).build());
                } else {
                    return CompletableFuture.completedFuture(RequestResponse.builder().data(new ArrayList()).httpStatus(HttpStatus.OK).build());
                }
            } else {
                return CompletableFuture.completedFuture(RequestResponse.builder().httpStatus(HttpStatus.NOT_FOUND).build());
            }
        }
    }

    /**
     * Returns the media type that is associated with the operation
     *
     * @param operation  the operation
     * @param acceptType the incoming request
     * @return the media type associated with the request of a default one if one hasn't been specified
     */
    private MediaType getMediaType(final Operation operation, final String acceptType) {
        MediaType mediaType = null;
        Optional<String> responseCode =
                operation.getResponses().keySet().parallelStream().filter(s -> s.matches(HttpStatusPatterns.SUCCESS)).findFirst();
        if (responseCode.isPresent()) {
            ApiResponse response = operation.getResponses().get(responseCode.get());
            mediaType = response.getContent().getOrDefault(acceptType,
                    OAS3MediaTypes.APPLICATION_JSON());
        }
        return mediaType;
    }

    /**
     * Process a HEAD request.
     *
     * @param path               the path of the request
     * @param operation          the operation from the API specification
     * @param httpServletRequest the incoming request to process
     * @return the response processing the HEAD request.
     */
    @SuppressWarnings("unused")
    private CompletableFuture<RequestResponse> processHed(final String path, final Operation operation,
                                                          final HttpServletRequest httpServletRequest) {
        return CompletableFuture.completedFuture(RequestResponse.builder().httpStatus(HttpStatus.NOT_IMPLEMENTED).build());
    }

    /**
     * Process an OPTION request.
     *
     * @param path               the path of the request
     * @param operation          the operation from the API specification
     * @param httpServletRequest the incoming request to process
     * @return the response from processing the OPTION request.
     */
    @SuppressWarnings("unused")
    private CompletableFuture<RequestResponse> processOpt(final String path, final Operation operation,
                                                          final HttpServletRequest httpServletRequest) {
        return CompletableFuture.completedFuture(RequestResponse.builder().httpStatus(HttpStatus.NOT_IMPLEMENTED).build());
    }

    /**
     * Processes a PATCH request.
     *
     * @param path               the path of the request
     * @param operation          the operation details from the API specification
     * @param httpServletRequest the request to process
     * @return the response from processing the PATCH request.
     */
    @SuppressWarnings("unused")
    private CompletableFuture<RequestResponse> processPch(final String path, final Operation operation,
                                                          final HttpServletRequest httpServletRequest) {
        return CompletableFuture.completedFuture(RequestResponse.builder().httpStatus(HttpStatus.NOT_IMPLEMENTED).build());
    }

    /**
     * Processes a POST and PUT request.
     *
     * @param path               the path of the request
     * @param operation          the operation details from the API specification
     * @param httpServletRequest the request to process
     * @return the response from processing a POST/PUT request.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
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
            }

            ObjectRepository<Map> repository = database.getRepository(Map.class);
            Cursor<Map> results = repository.find(ObjectFilters.and(filters.toArray(new ObjectFilter[0])));
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
                    if (repository.update(ObjectFilters.and(filters.toArray(new ObjectFilter[0])), data)
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
     * Processes a PUT request.
     *
     * @param path               the path of the request
     * @param operation          the operation details from the API specification
     * @param httpServletRequest the request to process
     * @return the response from processing a POST request.
     */
    private CompletableFuture<RequestResponse> processPst(final String path, Operation operation,
                                                          HttpServletRequest httpServletRequest) {
        return this.processPostPut(path, operation, httpServletRequest);
    }

    /**
     * Processes a PUT request.
     *
     * @param path               the path of the request
     * @param operation          the operation details from the API specification
     * @param httpServletRequest the request to process
     * @return the response from processing a PUT request.
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
     * Processes a TRACE request
     *
     * @param path               the path of the request
     * @param operation          the operation details from the API specification
     * @param httpServletRequest the request to process
     * @return the response from processing the TRACE request.
     */
    @SuppressWarnings("unused")
    private CompletableFuture<RequestResponse> processTce(final String path, final Operation operation,
                                                          final HttpServletRequest httpServletRequest) {
        return CompletableFuture.completedFuture(RequestResponse.builder().httpStatus(HttpStatus.NOT_IMPLEMENTED).build());
    }

}
