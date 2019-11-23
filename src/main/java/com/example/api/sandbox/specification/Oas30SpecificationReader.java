package com.example.api.sandbox.specification;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.example.api.sandbox.exception.SpecificationParsingException;
import com.example.api.sandbox.model.APISpecification;
import com.example.api.sandbox.model.OAS30APISpecification;

import io.swagger.v3.parser.OpenAPIV3Parser;

/**
 * 
 * 
 *
 */
public class Oas30SpecificationReader extends AbstractSpecificationReader {

    @Autowired
    private ApplicationContext applicationContext;
    
	@Override
	public APISpecification parse() throws SpecificationParsingException {
		try {
			OAS30APISpecification apiSpecification = (OAS30APISpecification) applicationContext.getBean("Oas30Specification");
			apiSpecification.setOpenApi(new OpenAPIV3Parser().read(specificationFile.getCanonicalPath()));
			return apiSpecification;
		} catch (IOException e) {
			throw new SpecificationParsingException(
					String.format("Unable to parse the specification, %s", e.getMessage()));
		}
	}

}
