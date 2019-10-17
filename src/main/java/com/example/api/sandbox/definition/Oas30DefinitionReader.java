package com.example.api.sandbox.definition;

import java.io.File;
import java.io.IOException;

import com.example.api.sandbox.exception.DefinitionParsingException;
import com.example.api.sandbox.model.APIDefinition;
import com.example.api.sandbox.model.OAS30APIDefinition;

import io.swagger.v3.parser.OpenAPIV3Parser;

/**
 * 
 * 
 *
 */
public class Oas30DefinitionReader extends AbstractDefinitionReader {

	public Oas30DefinitionReader(File definitionFile) {
		super(definitionFile);
	}

	@Override
	public APIDefinition parse() throws DefinitionParsingException {
		try {
			return new OAS30APIDefinition(new OpenAPIV3Parser().read(definitionFile.getCanonicalPath()));
		} catch (IOException e) {
			throw new DefinitionParsingException(
					String.format("Unable to parse the API definition, %s", e.getMessage()));
		}
	}

}
