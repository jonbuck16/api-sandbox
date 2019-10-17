package com.example.api.sandbox.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class AbstractAPIDefinition implements APIDefinition {
	private ModelType modelType;

}
