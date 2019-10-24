package com.example.api.sandbox.definition;

import java.io.File;

import lombok.Getter;
import lombok.Setter;

/**
 * Base reader for all definition readers
 * 
 *
 */
public abstract class AbstractDefinitionReader implements DefinitionReader {

	@Getter
	@Setter
	protected File definitionFile;

	public AbstractDefinitionReader() {
		super();
	}

	@Override
	public String toString() {
		return definitionFile.getName();
	}
	
}
