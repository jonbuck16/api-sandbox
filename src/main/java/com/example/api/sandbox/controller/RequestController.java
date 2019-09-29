package com.example.api.sandbox.controller;

import java.util.logging.Level;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.sandbox.service.ConfigService;

import lombok.extern.flogger.Flogger;

@RestController
@Flogger
public class RequestController {
	
	@Autowired
	private ConfigService configService;
	
	@GetMapping(value="/**")  
	public ResponseEntity<?> handleIncomingRequest() {
		log.at(Level.INFO).log("Processing request");
		
		// TODO - match the end-point to one in the parsed API definition, return 404 if one doesn't match
		// TODO - Assert any request parameters, headers and form data if put/post request, return 403 (Bad  Request) or specific error if one is defined for bad request in the api definition
		// TODO - Assess the return data model
		// TODO - Generate response data
		// TODO - Return response
		
		return ResponseEntity.ok().build();
	}
	
}
