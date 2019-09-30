package com.example.api.sandbox.service;

import java.util.logging.Level;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.api.sandbox.definition.DefinitionFactory;
import com.example.api.sandbox.exception.DefinitionParsingException;
import com.example.api.sandbox.model.APIDefinition;

import lombok.Getter;
import lombok.extern.flogger.Flogger;

/**
 * Responsible for loading and parsing the API definition at startup
 * 
 *
 */
@Service
@Flogger
public class DefinitionService {

	@Getter
	private APIDefinition apiDefinition;

	@Value("${definitions.directory}")
	private String definitionDirectory;

	@PostConstruct
	public void initialise() {
		try {
			log.at(Level.INFO).log("Parsing the API definition...");
			this.apiDefinition = DefinitionFactory.builder().definitionDir(definitionDirectory).build()
					.getDefinitionReader().parse();
			log.at(Level.INFO).log("API definition(s) parsed successfully...");
		} catch (DefinitionParsingException e) {
			log.at(Level.SEVERE).log("API definition(s) failed to parse!");
			log.at(Level.SEVERE).log("The error message was %s", e.getMessage());
		}
	}

}
