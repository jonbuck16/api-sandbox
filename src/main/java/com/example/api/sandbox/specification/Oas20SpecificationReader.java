package com.example.api.sandbox.specification;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.example.api.sandbox.exception.SpecificationParsingException;
import com.example.api.sandbox.model.APISpecification;
import com.example.api.sandbox.model.OAS20APISpecification;

import io.swagger.parser.SwaggerParser;

/**
 * Reads an Open API v2 (Swagger) specification and returns an appropriate
 * API Specification model.
 * 
 * @since v1
 */
public class Oas20SpecificationReader extends AbstractSpecificationReader {

	@Autowired
	private ApplicationContext applicationContext;
	
	@Override
	public APISpecification parse() throws SpecificationParsingException {
		try {
			OAS20APISpecification apiSpecification = (OAS20APISpecification) applicationContext.getBean("Oas20Specification");
			apiSpecification.setRaw(FileUtils.readFileToString(new File(specificationFile.getCanonicalPath()), StandardCharsets.UTF_8));
			apiSpecification.setSwagger(new SwaggerParser().read(specificationFile.getCanonicalPath()));
			return apiSpecification;
		} catch (IOException e) {
			throw new SpecificationParsingException(
					String.format("Unable to parse the specification, %s", e.getMessage()));
		}
	}

}
