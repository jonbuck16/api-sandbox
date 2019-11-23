package com.example.api.sandbox.specification;

import com.example.api.sandbox.exception.SpecificationParsingException;
import com.example.api.sandbox.model.AbstractAPISpecification;

/**
 * Reader that can read a Open API specification and return an associated
 * WebApiDocument.
 * 
 *
 */
public class Raml08SpecificationReader extends AbstractSpecificationReader {

	@Override
	public AbstractAPISpecification parse() throws SpecificationParsingException {
		throw new SpecificationParsingException("Not supported at present");
	}
	
}
