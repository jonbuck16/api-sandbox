package com.example.api.sandbox.utils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.swagger.models.Path;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;

public class SwaggerPathUtils {

	private final static Pattern variableKey = Pattern.compile("\\{([a-zA-Z]*)\\}");

	/**
	 * Converts the path from an API definition into a regular expression
	 * 
	 * @param path
	 * @param rawPath
	 * @return a new compiled Pattern
	 */
	public static Pattern pathToRegex(Path path, final String rawPath) {
		Matcher matcher = variableKey.matcher(rawPath);
		if (matcher.find()) {
			final String variableKeyValue = matcher.group(1);
			Optional<Parameter> optVariableParameter = path.getOperations().get(0).getParameters().stream()
					.filter(p -> p.getName().equals(variableKeyValue)).filter(p -> p.getIn().equalsIgnoreCase("path"))
					.findFirst();
			if (optVariableParameter.isPresent()) {
				PathParameter variableParameter = (PathParameter) optVariableParameter.get();
				switch (variableParameter.getType()) {
				case "integer":
					return Pattern.compile(rawPath.replaceAll("\\{[a-zA-Z0-9]*\\}", "[0-9]*"));
				default:
					return Pattern.compile(rawPath.replaceAll("\\{[a-zA-Z0-9]*\\}", "[a-zA-Z]*"));
				}
			} else {
				final String substiute = rawPath.replaceAll("\\{[a-zA-Z0-9]*\\}", "[a-zA-Z0-9]*");
				return Pattern.compile(substiute);
			}
		} else {
			return Pattern.compile(rawPath);
		}
	}

}
