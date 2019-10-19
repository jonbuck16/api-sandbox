package com.example.api.sandbox.model;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import lombok.Builder;
import lombok.Getter;

/**
 * 
 * @since v1
 */
@Builder
public class RequestResponse {
	@Getter private HttpStatus httpStatus;
	@Getter private Object data;
	
	public static RequestResponse EMPTY = RequestResponse.builder().httpStatus(HttpStatus.OK).data(StringUtils.EMPTY).build();
}
