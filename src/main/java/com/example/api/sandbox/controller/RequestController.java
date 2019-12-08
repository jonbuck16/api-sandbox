package com.example.api.sandbox.controller;

import com.example.api.sandbox.exception.RequestNotProcessedException;
import com.example.api.sandbox.model.RequestResponse;
import com.example.api.sandbox.service.SpecificationService;
import lombok.extern.flogger.Flogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

/**
 * The request controller is responsible for receiving and processing all
 * incoming requests.
 * <p>
 * This controller is a universal controller that is configured to receive all
 * requests that are fired into the application, once a request is received the
 * controller will use the Specification Service to match the signature of the
 * request to an end point defined in the API specification, assuming that one is
 * matched the controller will then assert what has been sent against the API
 * specification and then return a response based on the API specification and the
 * example data contained in the model.
 * </p>
 *
 * @since v1
 */
@RestController
@Flogger
public class RequestController {

    @Autowired
    private SpecificationService specificationsService;

    @GetMapping(value = "/**", produces = MediaType.ALL_VALUE)
    public ResponseEntity<?> handleIncomingGetRequest(final HttpServletRequest httpServletRequest) throws Throwable {
        return handleRequest(httpServletRequest);
    }

    @PostMapping(value = "/**", consumes = MediaType.ALL_VALUE, produces = MediaType.ALL_VALUE)
    public ResponseEntity<?> handleIncomingPostRequest(final HttpServletRequest httpServletRequest) throws Throwable {
        return handleRequest(httpServletRequest);
    }

    @PutMapping(value = "/**", produces = MediaType.ALL_VALUE)
    public ResponseEntity<?> handleIncomingPutRequest(final HttpServletRequest httpServletRequest) throws Throwable {
        return handleRequest(httpServletRequest);
    }

    @PatchMapping(value = "/**", produces = MediaType.ALL_VALUE)
    public ResponseEntity<?> handleIncomingPatchRequest(final HttpServletRequest httpServletRequest) throws Throwable {
        return handleRequest(httpServletRequest);
    }

    @DeleteMapping(value = "/**")
    public ResponseEntity<?> handleIncomingDeleteRequest(final HttpServletRequest httpServletRequest) throws Throwable {
        return handleRequest(httpServletRequest);
    }

    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> handleIncomingOptionsRequest(final HttpServletRequest httpServletRequest) throws Throwable {
        return handleRequest(httpServletRequest);
    }

    @RequestMapping(value = "/**", method = RequestMethod.HEAD, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> handleIncomingHeadRequest(final HttpServletRequest httpServletRequest) throws Throwable {
        return handleRequest(httpServletRequest);
    }

    /**
     * Handles the incoming request by passing it into the specifications service for processing.
     *
     * @param httpServletRequest in the incoming request
     * @return A response containing the data as defined by the API specification
     * @throws Throwable if something goeswrong and the request cannot be processed
     */
    private ResponseEntity<?> handleRequest(final HttpServletRequest httpServletRequest) throws Throwable {
        log.at(Level.INFO).log("Processing incoming request [%s] %s", httpServletRequest.getMethod(),
                httpServletRequest.getRequestURI());
        final CompletableFuture<RequestResponse> response = specificationsService.processRequest(httpServletRequest);
        RequestResponse requestResponse;
        try {
            requestResponse = response.get();
            log.at(Level.INFO).log("Request [%s] %s processed successfully", httpServletRequest.getMethod(),
                    httpServletRequest.getRequestURI());
            return new ResponseEntity<>(requestResponse.getData(), requestResponse.getHttpStatus());
        } catch (InterruptedException | ExecutionException e) {
            if (e.getCause() != null) {
                throw e.getCause();
            }
            throw new RequestNotProcessedException();
        }
    }

}
