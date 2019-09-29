package com.example.api.sandbox.definition;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import com.example.api.sandbox.exception.DefinitionParsingException;
import com.google.common.io.Files;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.flogger.Flogger;

/**
 * Factory which returns the appropriate definition reader for the API file
 * stored in the definition directory.
 * 
 *
 */
@Builder
@Flogger
public class DefinitionFactory {

	/**
	 * The directory where the API definition file will be located
	 */
	@Getter
	private String definitionDir;

	/**
	 * Returns the appropriate reader for the API definition file we find in the
	 * configuration directory.
	 * 
	 * @return a specific <code>DefinitionReader</code> for the API definition.
	 */
	public DefinitionReader getDefinitionReader() {
		// TODO Need to scan the definition directory to find the API definition
		// - Using the directory configured, list the files, pick the first file and
		// read the first line of the file
		// - Using the value of the first line of the file, determine which API we are
		// dealing with
		// - throw an exception if no API file is found
		// - throw an exception if there is a file but we cannot determine the type
		// - based on the first line of the API definition, instantiate and return the
		// reader
		final List<DefinitionReader> apiFiles = new LinkedList<>();
		for (File file : Files.fileTraverser().breadthFirst(new File(definitionDir))) {
			try {
				if (allowableFile(file)) {
					String firstLine = Files.asCharSource(file, StandardCharsets.UTF_8).readFirstLine();
					if (firstLine.contains("#%RAML 0.8")) {
						apiFiles.add(new Raml08DefinitionReader(file));
					} else if (firstLine.contains("#%RAML 1.0")) {
						apiFiles.add(new Raml10DefinitionReader(file));
					} else if (firstLine.contains("swagger: \"2")) {
						apiFiles.add(new Oas20DefinitionReader(file));
					} else if (firstLine.contains("openapi: \"3")) {
						apiFiles.add(new Oas30DefinitionReader(file));
					} else {
						log.at(Level.WARNING).log("%s is not an API file or not a file that we support, ignoring...",
								file);
					}
				}
			} catch (IOException e) {
				log.at(Level.WARNING).log(String.format("Unable to read the file %s", file.getName()));
			}
		}

		if (apiFiles.isEmpty())

		{
			throw new DefinitionParsingException("No API files found in the definition directory!");
		}

		if (apiFiles.size() > 1) {
			log.at(Level.INFO).log("More than one API file found...");
		}

		log.at(Level.INFO).log("Returning the API Definition %s", apiFiles.get(0));
		return apiFiles.get(0);
	}

	private boolean allowableFile(File file) {
		return file.isFile() && !file.getName().equalsIgnoreCase(".DS_Store");
	}
}
