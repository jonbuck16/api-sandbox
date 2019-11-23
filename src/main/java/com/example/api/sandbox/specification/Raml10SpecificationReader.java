package com.example.api.sandbox.specification;

import com.example.api.sandbox.exception.SpecificationParsingException;
import com.example.api.sandbox.model.AbstractAPISpecification;

/**
 * 
 * 
 *
 */
public class Raml10SpecificationReader extends AbstractSpecificationReader {

	@Override
	public AbstractAPISpecification parse() throws SpecificationParsingException {
		throw new SpecificationParsingException("Not supported at present");
	}

}
