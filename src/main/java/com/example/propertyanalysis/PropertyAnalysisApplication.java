package com.example.propertyanalysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PropertyAnalysisApplication {

	public static void main(String[] args) {
		SpringApplication.run(PropertyAnalysisApplication.class, args);
	}

}
