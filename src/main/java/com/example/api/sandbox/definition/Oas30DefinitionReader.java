package com.example.api.sandbox.definition;

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

	@Override
	public APIDefinition parse() throws DefinitionParsingException {
		try {
			OAS30APIDefinition apiDefinition = new OAS30APIDefinition();
			apiDefinition.setOpenApi(new OpenAPIV3Parser().read(definitionFile.getCanonicalPath()));
			return apiDefinition;
		} catch (IOException e) {
			throw new DefinitionParsingException(
					String.format("Unable to parse the API definition, %s", e.getMessage()));
		}
	}

}
