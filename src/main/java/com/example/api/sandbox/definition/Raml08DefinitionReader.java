package com.example.api.sandbox.definition;

import com.example.api.sandbox.exception.DefinitionParsingException;
import com.example.api.sandbox.model.AbstractAPIDefinition;

/**
 * Reader that can read a Open API definition and return an associated
 * WebApiDocument.
 * 
 *
 */
public class Raml08DefinitionReader extends AbstractDefinitionReader {

	@Override
	public AbstractAPIDefinition parse() throws DefinitionParsingException {
		throw new DefinitionParsingException("Not supported at present");
	}
	
}
