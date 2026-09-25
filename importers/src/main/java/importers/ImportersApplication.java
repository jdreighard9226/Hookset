package importers;

import importers.waterbody.controller.WaterBodyController;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EntityScan(basePackages = "shared.jpa.entity")
public class ImportersApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImportersApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(WaterBodyController waterBodyController) {
        return args -> {
            waterBodyController.importWaterBodies();
        };
    }
}