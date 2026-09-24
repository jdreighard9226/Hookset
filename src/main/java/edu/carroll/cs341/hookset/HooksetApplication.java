package edu.carroll.cs341.hookset;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "shared.jpa.entity")
public class HooksetApplication {

	public static void main(String[] args) {
		SpringApplication.run(HooksetApplication.class, args);
	}

}
