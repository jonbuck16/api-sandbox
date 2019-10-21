package com.example.api.sandbox.controller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.sandbox.exception.RequestNotProcessedException;
import com.example.api.sandbox.model.RequestResponse;
import com.example.api.sandbox.service.DefinitionService;

import lombok.extern.flogger.Flogger;

/**
 * The request controller is responsible for receiving and processing all
 * incoming requests.
 * <p>
 * This controller is a universal controller that is configured to receive all
 * requests that are fired into the application, once a request is received the
 * controller will use the Definitions Service to match the signature of the
 * request to an end point defined in the API definition, assuming that one is
 * matched the controller will then assert what has been sent against the API
 * definition and then return a response based on the API definition and the
 * example data contained in the model.
 * </p>
 *
 * @since v1
 */
@RestController
@Flogger
public class RequestController {

	@Autowired
	private DefinitionService definitionsService;
	

	@GetMapping(value = "/**", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<?> handleIncomingGetRequest(final HttpServletRequest httpServletRequest) throws Throwable {
		return handleRequest(httpServletRequest);
	}
	
	@PostMapping(value = "/**", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<?> handleIncomingPostRequest(final HttpServletRequest httpServletRequest) throws Throwable {
		return handleRequest(httpServletRequest);
	}
	
	@PutMapping(value = "/**", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<?> handleIncomingPutRequest(final HttpServletRequest httpServletRequest) throws Throwable {
		return handleRequest(httpServletRequest);
	}
	
	@PatchMapping(value = "/**", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<?> handleIncomingPatchRequest(final HttpServletRequest httpServletRequest) throws Throwable {
		return handleRequest(httpServletRequest);
	}
	
	@DeleteMapping(value = "/**")
	public ResponseEntity<?> handleIncomingDeleteRequest(final HttpServletRequest httpServletRequest) throws Throwable {
		return handleRequest(httpServletRequest);
	}
		
	/**
	 * 
	 * @param httpServletRequest
	 * @return
	 * @throws Throwable 
	 */
	private ResponseEntity<?> handleRequest(final HttpServletRequest httpServletRequest) throws Throwable {
		log.at(Level.INFO).log("Processing incoming request [%s] %s", httpServletRequest.getMethod(), httpServletRequest.getRequestURI());
		final CompletableFuture<RequestResponse> response = definitionsService.processRequest(httpServletRequest);
		RequestResponse requestResponse;
		try {
			requestResponse = response.get();
			log.at(Level.INFO).log("Request [%s] %s processed successfully", httpServletRequest.getMethod(), httpServletRequest.getRequestURI());
			return new ResponseEntity<>(requestResponse.getData(), requestResponse.getHttpStatus());
		} catch (InterruptedException | ExecutionException e) {
			if (e.getCause() != null) {
				throw e.getCause();
			}
			throw new RequestNotProcessedException();
		}
	}

}
