package com.example.api.sandbox.utils;

import com.example.api.sandbox.Constants;
import com.example.api.sandbox.exception.InvalidInputException;
import com.google.common.collect.ImmutableList;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Collection of utilities to support the Open API v3 specification standard.
 *
 * @since v1
 */
public class OpenApiPathUtils extends BasePathUtils {

    @SuppressWarnings("RegExpRedundantEscape")
    private final static Pattern variableKey = Pattern.compile("\\{([a-zA-Z]*)\\}");

    /**
     * Converts the path from the specification into a regular expression
     *
     * @param path       the request path
     * @param pathItem   the PathItem from the specification
     * @param httpMethod the method of the request
     * @return a new compiled regex pattern to use to match the incoming request.
     */
    public static Pattern pathToRegex(final String path, final PathItem pathItem, final HttpMethod httpMethod) {
        Matcher matcher = variableKey.matcher(path);
        String patternValue = path;
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
     * Validates that the request contains the required elements from the operation
     *
     * @param operation          the Operation to validate the request against
     * @param httpServletRequest the incoming request to validate
     */
    public static void validateOperation(final Operation operation, final HttpServletRequest httpServletRequest) {
        // Generate list of parameters to validate against, we will use this list to
        // 'tick off' which parameters exist in the request and therefore if anything is
        // left then that is missing
        if (operation.getParameters() != null) {
            final List<String> parametersToValidate = operation.getParameters().stream().map(Parameter::getName)
                    .collect(Collectors.toList());
            for (Parameter parameter : operation.getParameters()) {
                if (parameter.getRequired()) {
                    switch (parameter.getIn()) {
                        case Constants.COOKIE:
                            // TODO: requires implementation to validate
                            break;
                        case Constants.QUERY:
                            Enumeration<String> parameterNames = httpServletRequest.getParameterNames();
                            while (parameterNames.hasMoreElements()) {
                                final String parameterName = parameterNames.nextElement();
                                if (parameterName.equals(parameter.getName())) {
                                    parametersToValidate.remove(parameter.getName());
                                }
                            }
                            break;
                        case Constants.HEADER:
                            Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
                            while (headerNames.hasMoreElements()) {
                                final String headerName = headerNames.nextElement();
                                if (headerName.equals(parameter.getName())) {
                                    parametersToValidate.remove(parameter.getName());
                                }
                            }
                            break;
                        default:
                            // TODO as just removing is not good enough...
                            parametersToValidate.remove(parameter.getName());
                    }
                } else {
                    parametersToValidate.remove(parameter.getName());
                }
            }
            if (parametersToValidate.size() > 0) {
                throw new InvalidInputException(400, "The request is invalid!", parametersToValidate);
            }
        }

        if (operation.getRequestBody() != null && httpServletRequest.getContentLength() == 0) {
            throw new InvalidInputException(400, "The request is invalid!", ImmutableList.of("Request Body"));
        }
    }

}
