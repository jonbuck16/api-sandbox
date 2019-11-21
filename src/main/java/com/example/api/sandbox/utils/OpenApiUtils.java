package com.example.api.sandbox.utils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.example.api.sandbox.Constants;

import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.Parameter.StyleEnum;
import io.swagger.v3.oas.models.parameters.PathParameter;

/**
 * 
 * 
 * @since v1
 */
public class OpenApiUtils {

	private final static Pattern variableKey = Pattern.compile("\\{([a-zA-Z]*)\\}");
	
	/**
	 * Converts the path from an API definition into a regular expression
	 * 
	 * @param path
	 * @param rawPath
	 * @return a new compiled Pattern
	 */
	public static Pattern pathToRegex(final String rawPath, final PathItem pathItem, final HttpMethod httpMethod) {
        Matcher matcher = variableKey.matcher(rawPath);
        String patternValue = rawPath;
        while (matcher.find()) {
            final String variableKeyValue = matcher.group(1);
            if (pathItem.readOperationsMap().containsKey(httpMethod)) {
                Optional<Parameter> optVariableParameter = pathItem.readOperationsMap().get(httpMethod).getParameters().stream()
                        .filter(p -> p.getName().equals(variableKeyValue)).filter(p -> p.getIn().equalsIgnoreCase(Constants.PATH))
                        .findFirst();
                if (optVariableParameter.isPresent()) {
                    PathParameter variableParameter = (PathParameter) optVariableParameter.get();
                    if (StringUtils.lowerCase(variableParameter.getSchema().getType()).equals(Constants.INTEGER)) {
                        patternValue = patternValue.replace("{" + variableKeyValue + "}", "[0-9]*");
                    } else {
                        patternValue = patternValue.replace("{" + variableKeyValue + "}", "[a-zA-Z]*");
                    }
                } else {
                    patternValue = patternValue.replace("{" + variableKeyValue + "}", "\\{" + matcher.group(1) + "\\}");
                }
            } else {
                patternValue = patternValue.replace("{" + variableKeyValue + "}", "\\{" + matcher.group(1) + "\\}");
            }
        }
        return Pattern.compile(patternValue);
	}

	/**
	 * 
	 * @param style
	 * @return
	 */
    public static String getDelimeterForStyle(StyleEnum style) {
        switch (style) {
        case SPACEDELIMITED:
            return null;
        case DEEPOBJECT:
            return null;
        case FORM:
            return null;
        case LABEL:
            return null;
        case MATRIX:
            return null;
        case PIPEDELIMITED:
            return Constants.PIPE;
        default: // Simple
            return Constants.COMMA;
        }
    }
	
}
