package com.example.api.sandbox.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.example.api.sandbox.model.APISpecification;
import com.example.api.sandbox.model.OAS20APISpecification;
import com.example.api.sandbox.model.OAS30APISpecification;
import com.example.api.sandbox.specification.Oas20SpecificationReader;
import com.example.api.sandbox.specification.Oas30SpecificationReader;
import com.example.api.sandbox.specification.Raml08SpecificationReader;
import com.example.api.sandbox.specification.Raml10SpecificationReader;
import com.example.api.sandbox.specification.SpecificationReader;

@Configuration
public class SpecificationConfig {

	@Bean(name = "Raml08Reader")
	@Scope("prototype")
	public SpecificationReader raml08SpecificationReader() {
		return new Raml08SpecificationReader();
	}
	
	@Bean(name = "Raml08Specification")
	@Scope("prototype")
	public APISpecification raml08Defnition() {
		return null;
	}
	
	@Bean(name = "Raml10Reader")
	@Scope("prototype")
	public SpecificationReader raml10SpecificationReader() {
		return new Raml10SpecificationReader();
	}
	
	@Bean(name = "Raml10Specification")
	@Scope("prototype")
	public APISpecification raml10Defnition() {
		return null;
	}
	
	@Bean(name = "Oas20Reader")
	@Scope("prototype")
	public SpecificationReader oas20SpecificationReader() {
		return new Oas20SpecificationReader();
	}
	
	@Bean(name = "Oas20Specification")
	@Scope("prototype")
	public APISpecification oas20Specification() {
		return new OAS20APISpecification();
	}
	
	@Bean(name = "Oas30Reader")
	@Scope("prototype")
	public SpecificationReader oas30SpecificationReader() {
		return new Oas30SpecificationReader();
	}
	
	@Bean(name = "Oas30Specification")
	@Scope("prototype")
	public APISpecification oas30Specification() {
		return new OAS30APISpecification();
	}
	
}
