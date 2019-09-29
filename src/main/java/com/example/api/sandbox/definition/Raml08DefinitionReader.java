package com.example.api.sandbox.definition;

import java.io.File;

import com.example.api.sandbox.exception.DefinitionParsingException;
import com.example.api.sandbox.model.APIDefinition;

/**
 * Reader that can read a Open API definition and return an associated
 * WebApiDocument.
 * 
 *
 */
public class Raml08DefinitionReader extends AbstractDefinitionReader {

	public Raml08DefinitionReader(final File definitionFile) {
		super(definitionFile);
	}

	@Override
	public APIDefinition parse() throws DefinitionParsingException {
		throw new DefinitionParsingException("Not supported at present");
	}
	
}
