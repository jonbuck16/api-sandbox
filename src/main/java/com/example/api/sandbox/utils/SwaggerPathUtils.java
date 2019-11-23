package com.example.api.sandbox.utils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.example.api.sandbox.Constants;

import io.swagger.models.HttpMethod;
import io.swagger.models.Path;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;

/**
 * Collection of utilities that support the parsing of Swagger API Specifications.
 * 
 * @since v1
 */
public class SwaggerPathUtils {

    private final static Pattern variableKey = Pattern.compile("\\{([a-zA-Z]*)\\}");

    /**
     * Converts the path from an API specification into a regular expression
     * 
     * @param path
     * @param rawPath
     * @param httpMethod
     * @return a new compiled Pattern
     */
    public static Pattern pathToRegex(final String rawPath, final Path path, final HttpMethod httpMethod) {
        Matcher matcher = variableKey.matcher(rawPath);
        String patternValue = rawPath;
        while (matcher.find()) {
            final String variableKeyValue = matcher.group(1);
            if (path.getOperationMap().containsKey(httpMethod)) {
                Optional<Parameter> optVariableParameter = path.getOperationMap().get(httpMethod).getParameters().stream()
                        .filter(p -> p.getName().equals(variableKeyValue)).filter(p -> p.getIn().equalsIgnoreCase(Constants.PATH))
                        .findFirst();
                if (optVariableParameter.isPresent()) {
                    PathParameter variableParameter = (PathParameter) optVariableParameter.get();
                    if (StringUtils.lowerCase(variableParameter.getType()).equals(Constants.INTEGER)) {
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

}
