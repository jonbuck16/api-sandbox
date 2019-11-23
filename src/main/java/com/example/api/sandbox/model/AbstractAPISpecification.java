package com.example.api.sandbox.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class AbstractAPISpecification implements APISpecification {
	private ModelType modelType;

}
