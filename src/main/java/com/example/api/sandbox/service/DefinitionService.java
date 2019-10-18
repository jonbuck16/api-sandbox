package com.example.api.sandbox.service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ResponseStatusException;

import com.example.api.sandbox.definition.DefinitionFactory;
import com.example.api.sandbox.exception.DefinitionParsingException;
import com.example.api.sandbox.exception.RequestNotFoundException;
import com.example.api.sandbox.model.APIDefinition;

import lombok.Getter;
import lombok.extern.flogger.Flogger;

/**
 * Responsible for loading and parsing the API definition at startup
 * 
 *
 */
@Service
@Flogger
public class DefinitionService {

	@Getter
	private APIDefinition apiDefinition;

	@Value("${definitions.directory}")
	private String definitionDirectory;

	@PostConstruct
	public void initialise() {
		try {
			log.atInfo().log("Looking for API definitions...");
			this.apiDefinition = DefinitionFactory.builder().definitionDir(definitionDirectory).build()
					.getDefinitionReader().parse();
			log.atInfo().log("API definition(s) parsed successfully...");
		} catch (DefinitionParsingException e) {
			log.atSevere().log("API definition(s) failed to parse!");
			log.atSevere().log("The error message was %s", e.getMessage());
		}
	}

	/**
	 * Processes the incoming HTTP request and returns an appropriate response.
	 * <p>
	 * <ul>
	 * <li>match the end-point (requestURI) to one in the parsed API definition and
	 * return 404 if one doesn't match, this will need to take into account path
	 * variables such as GET /customer/12345 and/or query parameters such as GET
	 * /customer?customerNum=12345 for example, the first example will be defined as
	 * /customer/{customerNum}: in the API definition</li>
	 * <li>My feeling is that the definitions service could contain that logic</li>
	 * <li>We need to assert any request parameters, headers and form data if
	 * put/post request, query parameters etc. return 403 (Bad Request) or specific
	 * error if one is defined for bad request in the API definition</li>
	 * <li>Determine what the return data model looks like based on the API
	 * definition for the incoming end point.</li>
	 * <li>Generate the appropriate response data.</li>
	 * <li>Once the response has been processed the response data should be placed
	 * into the internal in memory database such that if someone was to try and
	 * recall that data it would be returned as it was sent in (SMART).</li>
	 * <li>We can use NitriteDB which is an in memory NoSql database, perfect for
	 * JSON data.</li>
	 * <li>Return that response data in the appropriate form to the caller.</li>
	 * </ul>
	 * 
	 * @param httpRequest the incoming HttpServletRequest to process
	 */
	public Object processRequest(final HttpServletRequest httpRequest) {
		try {
			return apiDefinition.processRequest(httpRequest);
		} catch (RequestNotFoundException ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found!", ex);
		}
	}

}
