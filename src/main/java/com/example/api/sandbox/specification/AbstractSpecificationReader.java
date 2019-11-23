package com.example.api.sandbox.specification;

import java.io.File;

import lombok.Getter;
import lombok.Setter;

/**
 * Base reader for all specification readers
 * 
 *
 */
public abstract class AbstractSpecificationReader implements SpecificationReader {

	@Getter
	@Setter
	protected File specificationFile;

	public AbstractSpecificationReader() {
		super();
	}

	@Override
	public String toString() {
		return specificationFile.getName();
	}
	
}
