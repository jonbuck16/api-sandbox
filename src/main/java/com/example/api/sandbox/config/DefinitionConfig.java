package com.example.api.sandbox.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.example.api.sandbox.definition.DefinitionReader;
import com.example.api.sandbox.definition.Oas20DefinitionReader;
import com.example.api.sandbox.definition.Oas30DefinitionReader;
import com.example.api.sandbox.definition.Raml08DefinitionReader;
import com.example.api.sandbox.definition.Raml10DefinitionReader;
import com.example.api.sandbox.model.APIDefinition;
import com.example.api.sandbox.model.OAS20APIDefinition;
import com.example.api.sandbox.model.OAS30APIDefinition;

@Configuration
public class DefinitionConfig {

	@Bean(name = "Raml08Reader")
	@Scope("prototype")
	public DefinitionReader raml08DefinitionReader() {
		return new Raml08DefinitionReader();
	}
	
	@Bean(name = "Raml08Definition")
	@Scope("prototype")
	public APIDefinition raml08Defnition() {
		return null;
	}
	
	@Bean(name = "Raml10Reader")
	@Scope("prototype")
	public DefinitionReader raml10Definitionreader() {
		return new Raml10DefinitionReader();
	}
	
	@Bean(name = "Raml10Definition")
	@Scope("prototype")
	public APIDefinition raml10Defnition() {
		return null;
	}
	
	@Bean(name = "Oas20Reader")
	@Scope("prototype")
	public DefinitionReader oas20DefinitionReader() {
		return new Oas20DefinitionReader();
	}
	
	@Bean(name = "Oas20Definition")
	@Scope("prototype")
	public APIDefinition oas20Definition() {
		return new OAS20APIDefinition();
	}
	
	@Bean(name = "Oas30Reader")
	@Scope("prototype")
	public DefinitionReader oas30DefinitionReader() {
		return new Oas30DefinitionReader();
	}
	
	@Bean(name = "Oas30Definition")
	@Scope("prototype")
	public APIDefinition oas30Definition() {
		return new OAS30APIDefinition();
	}
	
}
