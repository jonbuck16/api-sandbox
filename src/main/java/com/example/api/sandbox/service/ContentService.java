package com.example.api.sandbox.service;

import com.example.api.sandbox.exception.RequestNotProcessedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

/**
 * @since v1
 */
public class ContentService {

    private static XmlMapper xmlMapper;

    static {
        JacksonXmlModule xmlModule = new JacksonXmlModule();
        xmlModule.setDefaultUseWrapper(false);
        xmlMapper = new XmlMapper(xmlModule);
    }

    /**
     * Converts the data into the target type as set by the Accept header
     *
     * @param acceptType the content type to return the data in
     * @param toList     the list of items to convert to the target type
     * @return the data in the desired format
     * @throws RequestNotProcessedException if the data cannot be processed
     */
    @SuppressWarnings("rawtypes")
    public static Object asType(final String acceptType, Map toList) throws RequestNotProcessedException {
        if (MediaType.APPLICATION_XML_VALUE.equalsIgnoreCase(acceptType)) {
            try {
                return xmlMapper.writer().withRootName("").writeValueAsString(toList);
            } catch (JsonProcessingException e) {
                throw new RequestNotProcessedException(e.getMessage());
            }
        }
        return toList;
    }

    /**
     * @param accept
     * @param toObject
     * @return
     * @throws RequestNotProcessedException
     */
    @SuppressWarnings("rawtypes")
    public static Object asType(String accept, List<Map> toObject) throws RequestNotProcessedException {
        return ContentService.asType(accept, "Items", toObject);
    }

    /**
     * Converst the data object into the target type as set by the Accept header
     *
     * @param accept
     * @param root    the data type in which to return the data
     * @param mapList the object to convert
     * @return the data in the desired format
     * @throws RequestNotProcessedException
     */
    @SuppressWarnings("rawtypes")
    public static Object asType(String accept, String root, List<Map> mapList) throws RequestNotProcessedException {
        if (MediaType.APPLICATION_XML_VALUE.equalsIgnoreCase(accept)) {
            try {
                return xmlMapper.writer().withRootName(root).writeValueAsString(mapList);
            } catch (JsonProcessingException e) {
                throw new RequestNotProcessedException(e.getMessage());
            }
        }
        return mapList;
    }
}
