package com.example.api.sandbox.definition;

import java.io.File;

import com.example.api.sandbox.exception.DefinitionParsingException;
import com.example.api.sandbox.model.APIDefinition;

/**
 * Responsible for parsing an API definition file and returning the model.
 * 
 *
 */
public interface DefinitionReader {

	public APIDefinition parse() throws DefinitionParsingException;
	
	public void setDefinitionFile(final File file);
	
}
