package com.example.api.sandbox.config;

import org.dizitart.no2.Nitrite;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class containing to support the in-memory database.
 * <p>
 * The in memory database is used to support the ability to capture and contain
 * data that is specific to an end-point thus that if a post, put or get is
 * enacted then the application will return consistent data.
 * </p>
 * 
 */
@Configuration
public class DatabaseConfig {

	@Bean(name = "database")
	public Nitrite getDatabase() {
		return Nitrite.builder().openOrCreate();
	}

}
