package edu.duke.ece651;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "edu.duke.ece651.Controller",
    "edu.duke.ece651.dto",
    "edu.duke.ece651.simulationserver",
    "edu.duke.ece651.Config"
})
public class SimulationApp {
    public static void main(String[] args) {
        SpringApplication.run(SimulationApp.class, args);
    }
}
