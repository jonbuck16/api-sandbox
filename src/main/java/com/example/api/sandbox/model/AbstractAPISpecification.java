package com.example.api.sandbox.model;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;

@Data
public abstract class AbstractAPISpecification implements APISpecification {
	private ModelType modelType;
	private String raw = StringUtils.EMPTY;
	
	public AbstractAPISpecification(final ModelType modelType) {
	    this.modelType = modelType;
	}
}
