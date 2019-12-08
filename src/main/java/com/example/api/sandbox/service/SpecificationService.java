package com.example.api.sandbox.service;

import com.example.api.sandbox.exception.EndpointNotFoundException;
import com.example.api.sandbox.exception.SpecificationParsingException;
import com.example.api.sandbox.model.APISpecification;
import com.example.api.sandbox.model.RequestResponse;
import com.example.api.sandbox.specification.SpecificationFactory;
import lombok.Getter;
import lombok.extern.flogger.Flogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;

/**
 * Responsible for loading and parsing the API specification at startup
 * 
 *
 */
@Service
@Flogger
public class SpecificationService {

	@Getter
	private APISpecification apiSpecification;

	@Value("${specifications.directory}")
	private String specificationsDirectory;

	@Autowired
	private ApplicationContext applicationContext;

	@PostConstruct
	public void initialise() {
		try {
			log.atInfo().log("Looking for API specifications...");
			this.apiSpecification = SpecificationFactory.builder().specificationDir(specificationsDirectory)
					.applicationContext(applicationContext).build().getSpecificationReader().parse();
			log.atInfo().log("API specification parsed successfully...");
		} catch (SpecificationParsingException e) {
			log.atSevere().log("API specifications failed to parse!");
			log.atSevere().log("The error message was %s", e.getMessage());
		}
	}

	/**
	 * Processes the incoming HTTP request and returns an appropriate response.
	 * <p>
	 * <ul>
	 * <li>match the end-point (requestURI) to one in the parsed API specification and
	 * return 404 if one doesn't match, this will need to take into account path
	 * variables such as GET /customer/12345 and/or query parameters such as GET
	 * /customer?customerNum=12345 for example, the first example will be defined as
	 * /customer/{customerNum}: in the API specification</li>
	 * <li>My feeling is that the specifications service could contain that logic</li>
	 * <li>We need to assert any request parameters, headers and form data if
	 * put/post request, query parameters etc. return 403 (Bad Request) or specific
	 * error if one is defined for bad request in the API specification</li>
	 * <li>Determine what the return data model looks like based on the API
	 * specification for the incoming end point.</li>
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
	@Async(value = "RequestExecutor")
	public CompletableFuture<RequestResponse> processRequest(final HttpServletRequest httpRequest) {
		try {
			return apiSpecification.processRequest(httpRequest);
		} catch (EndpointNotFoundException ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found!", ex);
		}
	}

}
