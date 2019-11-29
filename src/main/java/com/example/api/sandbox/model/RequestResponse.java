package com.example.api.sandbox.model;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

/**
 * @since v1
 */
@Builder
public class RequestResponse {
	@Getter
	private HttpStatus httpStatus;
	@Getter
	private Object data;

	public static final RequestResponse EMPTY = RequestResponse.builder().httpStatus(HttpStatus.OK).data(StringUtils.EMPTY).build();
}
