package com.example.api.sandbox.utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.dizitart.no2.objects.ObjectFilter;
import org.dizitart.no2.objects.filters.ObjectFilters;

import com.example.api.sandbox.Constants;

public class BasePathUtils {

    /**
     * Retrieves the value for the request for the specified value.
     * 
     * @param parameter          the parameter object from the API specification
     * @param operation          the operation involved
     * @param httpServletRequest in incoming request from which to try and get data
     * 
     * @return an ObjectFilter
     */
    public static ObjectFilter constructObjectFilter(final String path, final String in, final String name, final String type,
            final HttpServletRequest httpServletRequest) {
        switch (in) {
        case Constants.QUERY:
            final String[] parameterValues = httpServletRequest.getParameterValues(name);
            List<ObjectFilter> filters = new LinkedList<>();
            Arrays.asList(parameterValues).forEach(s -> filters.add(ObjectFilters.eq(name, s)));
            return ObjectFilters.or(filters.toArray(new ObjectFilter[filters.size()]));
        case Constants.HEADER:
            return ObjectFilters.eq(name, httpServletRequest.getHeader(name));
        default: // path
            return handlePathValue(path, name, type, httpServletRequest);
        }
    }

    /**
     * Extracts a path variable value from the incoming request.
     * 
     * @param path
     * @param parameter
     * @param httpServletRequest
     * @return
     */
    private static ObjectFilter handlePathValue(final String path, final String name, final String type,
            final HttpServletRequest httpServletRequest) {
        final List<String> pathParts = Arrays.asList(path.split("/"));
        final List<String> requestParts = Arrays.asList(httpServletRequest.getRequestURI().split("/"));
        switch (type) {
        case Constants.ARRAY:
            // We really need a real world example to complete this type as I am unsure how
            // this would work in real life, for the moment this is my best guess.
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
