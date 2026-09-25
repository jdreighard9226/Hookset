package edu.carroll.cs341.hookset;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

/**
 * Entry point for the Hookset web application.
 *
 * <p>This application serves the Hookset website, including the home,
 * login, and signup pages, and handles authentication through Spring
 * Security.</p>
 *
 * <p>Entities are shared with the importers application through the
 * shared module, so entity scanning is pointed at that package.</p>
 */
@SpringBootApplication
// entities live in the shared module, outside this application's base package
@EntityScan(basePackages = "shared.jpa.entity")
public class HooksetApplication {

    /**
     * Starts the Hookset web application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(HooksetApplication.class, args);
    }
}