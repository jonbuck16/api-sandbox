package com.example.api.sandbox.specification;

import com.example.api.sandbox.exception.SpecificationParsingException;
import com.google.common.io.Files;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.flogger.Flogger;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

/**
 * Factory which returns the appropriate specification reader for the API file
 * stored in the specification directory.
 * 
 *
 */
@Builder
@Flogger
public class SpecificationFactory {

	/**
	 * The directory where the API specification file will be located
	 */
	@Getter
	private String specificationDir;

	private ApplicationContext applicationContext;

	/**
	 * Returns the appropriate reader for the API specification file we find in the
	 * configuration directory.
	 * 
	 * @return a specific <code>specificationReader</code> for the API specification.
	 */
	public SpecificationReader getSpecificationReader() {
		log.atInfo().log(String.format("Using the specifications folder %s", specificationDir));
		final List<SpecificationReader> apiFiles = new LinkedList<>();
		for (File file : Files.fileTraverser().breadthFirst(new File(specificationDir))) {
			try {
				if (allowableFile(file)) {
					final String firstLine = Files.asCharSource(file, StandardCharsets.UTF_8).readFirstLine();
					if (firstLine.contains("#%RAML 0.8")) {
						SpecificationReader raml08SpecificationReader = (SpecificationReader) applicationContext
								.getBean("Raml08Reader");
						raml08SpecificationReader.setSpecificationFile(file);
						apiFiles.add(raml08SpecificationReader);
					} else if (firstLine.contains("#%RAML 1.0")) {
						SpecificationReader raml10SpecificationReader = (SpecificationReader) applicationContext
								.getBean("Raml10Reader");
						raml10SpecificationReader.setSpecificationFile(file);
						apiFiles.add(raml10SpecificationReader);
					} else if (firstLine.contains("swagger: \"2")) {
						SpecificationReader oas20SpecificationReader = (SpecificationReader) applicationContext
								.getBean("Oas20Reader");
						oas20SpecificationReader.setSpecificationFile(file);
						apiFiles.add(oas20SpecificationReader);
					} else if (firstLine.contains("openapi: 3")) {
						SpecificationReader oas30SpecificationReader = (SpecificationReader) applicationContext
								.getBean("Oas30Reader");
						oas30SpecificationReader.setSpecificationFile(file);
						apiFiles.add(oas30SpecificationReader);
					} else {
						log.at(Level.WARNING).log("%s is not an API file or not a file that we support, ignoring...",
								file);
					}
				}
			} catch (IOException e) {
				log.atWarning().log(String.format("Unable to read the file %s", file.getName()));
			}
		}

		if (apiFiles.isEmpty()) {
			throw new SpecificationParsingException("No API files found in the specification directory!");
		}

		if (apiFiles.size() > 1) {
			log.atWarning().log("More than one API file found, only the first API specification will be processed...");
		}

		log.atInfo().log("Using the specification '%s'", apiFiles.get(0));
		return apiFiles.get(0);
	}

	private boolean allowableFile(File file) {
		return file.isFile() && !file.getName().equalsIgnoreCase(".DS_Store");
	}
}
