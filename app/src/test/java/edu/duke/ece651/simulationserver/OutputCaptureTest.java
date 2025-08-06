package edu.duke.ece651.simulationserver;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;

public class OutputCaptureTest {

  @Test
  public void testOutputCapture_basicFunctionality() {
    OutputCapture outputCapture = new OutputCapture();
    
    // Save the original System.out
    PrintStream originalOut = System.out;

    // Start capturing
    outputCapture.start();
    System.out.print("Hello, World!");

    // Verify that output is captured
    String captured = outputCapture.getOutput();
    assertTrue(captured.contains("Hello, World!"));

    // Clear output and verify it's empty
    outputCapture.clearOutput();
    assertEquals("", outputCapture.getOutput());

    // Stop capturing
    outputCapture.stop();

    // Verify System.out is restored
    assertSame(originalOut, System.out);
  }

  @Test
  public void testMultipleStartStop() {
    OutputCapture outputCapture = new OutputCapture();
    outputCapture.start();
    System.out.print("First capture.");
    outputCapture.stop();

    String firstCapture = outputCapture.getOutput();
    assertTrue(firstCapture.contains("First capture."));

    outputCapture.clearOutput();
    assertEquals("", outputCapture.getOutput());

    outputCapture.start();
    System.out.print("Second capture.");
    outputCapture.stop();

    String secondCapture = outputCapture.getOutput();
    assertTrue(secondCapture.contains("Second capture."));
  }
}
