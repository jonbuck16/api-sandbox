package com.example.api.sandbox.definition;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

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

	@Autowired
	private ApplicationContext applicationContext;
	
	@Override
	public APIDefinition parse() throws DefinitionParsingException {
		try {
			OAS20APIDefinition apiDefinition = (OAS20APIDefinition) applicationContext.getBean("Oas20Definition");
			apiDefinition.setSwagger(new SwaggerParser().read(definitionFile.getCanonicalPath()));
			return apiDefinition;
		} catch (IOException e) {
			throw new DefinitionParsingException(
					String.format("Unable to parse the API definition, %s", e.getMessage()));
		}
	}

}
