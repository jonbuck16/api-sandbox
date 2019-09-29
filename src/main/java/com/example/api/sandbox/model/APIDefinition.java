package com.example.api.sandbox.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class APIDefinition {
	private ModelType modelType;
	private Object model;
}
