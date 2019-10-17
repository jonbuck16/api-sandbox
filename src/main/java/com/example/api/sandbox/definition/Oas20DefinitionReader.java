package com.example.api.sandbox.definition;

import java.io.File;
import java.io.IOException;

import com.example.api.sandbox.exception.DefinitionParsingException;
import com.example.api.sandbox.model.APIDefinition;
import com.example.api.sandbox.model.OAS20APIDefinition;

import io.swagger.parser.SwaggerParser;

/**
 * Reads an Open API v2 (Swagger) definition and returns an appropriate
 * APIDefinition model.
 * 
 * @since v1
 */
public class Oas20DefinitionReader extends AbstractDefinitionReader {

	public Oas20DefinitionReader(final File definitionFile) {
		super(definitionFile);
	}

	@Override
	public APIDefinition parse() throws DefinitionParsingException {
		try {
			return new OAS20APIDefinition(new SwaggerParser().read(definitionFile.getCanonicalPath()));
		} catch (IOException e) {
			throw new DefinitionParsingException(
					String.format("Unable to parse the API definition, %s", e.getMessage()));
		}
	}

}
