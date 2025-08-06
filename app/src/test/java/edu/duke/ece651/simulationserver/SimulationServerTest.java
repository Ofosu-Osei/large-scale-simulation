package edu.duke.ece651.simulationserver;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class SimulationServerTest {
  @Test
  public void test_() {
    SimulationServer classUnderTest = new SimulationServer();
        assertNotNull(classUnderTest.getGreeting(), "app should have a greeting");
    }

}




