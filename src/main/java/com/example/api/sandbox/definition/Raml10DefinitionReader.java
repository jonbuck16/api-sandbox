package com.example.api.sandbox.definition;

import java.io.File;

import com.example.api.sandbox.exception.DefinitionParsingException;
import com.example.api.sandbox.model.AbstractAPIDefinition;

/**
 * 
 * 
 *
 */
public class Raml10DefinitionReader extends AbstractDefinitionReader {

	public Raml10DefinitionReader(final File definitionFile) {
		super(definitionFile);
	}

	@Override
	public AbstractAPIDefinition parse() throws DefinitionParsingException {
		throw new DefinitionParsingException("Not supported at present");
	}

}
