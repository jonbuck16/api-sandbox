package com.example.api.sandbox.controller;

import java.util.logging.Level;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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

	@GetMapping(value = "/**")
	public ResponseEntity<?> handleIncomingRequest() {
		log.at(Level.INFO).log("Processing request");

		// TODO
		// - match the end-point to one in the parsed API definition, return 404 if one
		//   doesn't match
		// - My feeling is that the definitions service could contain those matching
		//   methods
		// - We need to assert any request parameters, headers and form data if put/post
		//   request, return 403 (Bad Request) or specific error if one is defined for bad
		//   request in the api definition
		// - Determine what the return data model looks like based on the API definition
		//   for the incoming end point.
		// - Generate the appropriate response data
		// - Return that response data in the appropriate form

		return ResponseEntity.ok().build();
	}

}
