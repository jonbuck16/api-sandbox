package com.example.api.sandbox.specification;

import java.io.File;

import com.example.api.sandbox.exception.SpecificationParsingException;
import com.example.api.sandbox.model.APISpecification;

/**
 * Responsible for parsing an API specification file and returning the model.
 * 
 *
 */
public interface SpecificationReader {

	public APISpecification parse() throws SpecificationParsingException;
	
	public void setSpecificationFile(final File file);
	
}
