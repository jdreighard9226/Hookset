package importers;

import importers.waterbody.controller.WaterBodyAndFishController;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;

/**
 * Entry point for the Hookset importers application.
 *
 * <p>This application runs as a command line process that pulls outside data
 * into the Hookset database. On startup it imports water body and fish data
 * from the Montana FWP Fish Distribution API.</p>
 *
 * <p>Entities are shared with the main Hookset application through the
 * shared module, so entity scanning is pointed at that package.</p>
 *
 * <p><b>Sources Used:</b></p>
 * <ul>
 *   <li>Spring Boot CommandLineRunner API:
 *   https://docs.spring.io/spring-boot/api/java/org/springframework/boot/CommandLineRunner.html</li>
 * </ul>
 */
@SpringBootApplication
@EntityScan(basePackages = "shared.jpa.entity")
public class ImportersApplication {

    /**
     * Starts the importers application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ImportersApplication.class, args);
    }

    /**
     * Creates a command line runner that starts the water body and fish
     * import once the application context has loaded.
     *
     * @param waterBodyAndFishController the controller used to run the import
     * @return the command line runner that performs the import
     */
    @Bean
    public CommandLineRunner run(WaterBodyAndFishController waterBodyAndFishController) {
        return args -> {
            // runs the import once the application context has loaded
            waterBodyAndFishController.importWaterBodiesAndFish();
        };
    }
}
