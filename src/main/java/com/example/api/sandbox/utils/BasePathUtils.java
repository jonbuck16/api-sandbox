package com.example.api.sandbox.utils;

import com.example.api.sandbox.Constants;
import org.dizitart.no2.objects.ObjectFilter;
import org.dizitart.no2.objects.filters.ObjectFilters;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class BasePathUtils {

    /**
     * Retrieves the value for the parameter and returns an object filter which is used to search for the record.
     *
     * @param path               the parameter object from the API specification
     * @param in                 the location of the parameter
     * @param name               the name of the parameter
     * @param type               the data type of the parameter
     * @param httpServletRequest in incoming request from which to try and get data
     * @return an ObjectFilter that contains the parameter name and value to search for.
     */
    public static ObjectFilter constructObjectFilter(final String path, final String in, final String name, final String type,
                                                     final HttpServletRequest httpServletRequest) {
        switch (in) {
            case Constants.QUERY:
                final String[] parameterValues = httpServletRequest.getParameterValues(name);
                List<ObjectFilter> filters = new LinkedList<>();
                Arrays.asList(parameterValues).forEach(s -> filters.add(ObjectFilters.eq(name, s)));
                return ObjectFilters.or(filters.toArray(new ObjectFilter[0]));
            case Constants.HEADER:
                return ObjectFilters.eq(name, httpServletRequest.getHeader(name));
            default: // path
                return handlePathValue(path, name, type, httpServletRequest);
        }
    }

    /**
     * Extracts a path variable value from the incoming request.
     *
     * @param path               the path of the request
     * @param name               the name of the parameter
     * @param type               the data type of the parameter
     * @param httpServletRequest the incoming request from which to get the parameter value.
     * @return an object filter from which to search the data.
     */
    @SuppressWarnings("RedundantCast")
    private static ObjectFilter handlePathValue(final String path, final String name, final String type,
                                                final HttpServletRequest httpServletRequest) {
        final List<String> pathParts = Arrays.asList(path.split("/"));
        final List<String> requestParts = Arrays.asList(httpServletRequest.getRequestURI().split("/"));
        switch (type) {
            case Constants.ARRAY:
                // We really need a real world example to complete this implementation type as I am unsure how
                // this would work in real life, for the moment this is a best guess.
                return ObjectFilters.in(name, (Object[]) requestParts.get(pathParts.indexOf(String.format("{%s}", name))).split(","));
            case Constants.BOOLEAN:
                return ObjectFilters.eq(name, Boolean.valueOf(requestParts.get(pathParts.indexOf(String.format("{%s}", name)))));
            case Constants.INTEGER:
                return ObjectFilters.eq(name, Integer.valueOf(requestParts.get(pathParts.indexOf(String.format("{%s}", name)))));
            default:
                return ObjectFilters.eq(name, requestParts.get(pathParts.indexOf(String.format("{%s}", name))));
        }
    }

}
