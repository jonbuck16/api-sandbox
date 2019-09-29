package com.example.api.sandbox.definition;

import java.io.File;
import java.io.IOException;

import com.example.api.sandbox.exception.DefinitionParsingException;
import com.example.api.sandbox.model.APIDefinition;
import com.example.api.sandbox.model.ModelType;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;

/**
 * 
 * 
 *
 */
public class Oas20DefinitionReader extends AbstractDefinitionReader {

	public Oas20DefinitionReader(final File definitionFile) {
		super(definitionFile);
	}

	@Override
	public APIDefinition parse() throws DefinitionParsingException {
		try {
			Swagger openAPI = new SwaggerParser().read(definitionFile.getCanonicalPath());
			return new APIDefinition(ModelType.OAS2, openAPI);
		} catch (IOException e) {
			throw new DefinitionParsingException(
					String.format("Unable to parse the API definition, %s", e.getMessage()));
		}
	}

}
