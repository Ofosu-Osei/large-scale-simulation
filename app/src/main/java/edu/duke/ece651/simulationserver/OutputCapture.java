package edu.duke.ece651.simulationserver;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class OutputCapture {
  private final ByteArrayOutputStream baos;
  private final PrintStream originalOut;
  private final PrintStream captureOut;

  public OutputCapture() {
    this.baos = new ByteArrayOutputStream();
    this.captureOut = new PrintStream(baos);
    this.originalOut = System.out;
  }

  // start capturing System.out
  public void start() {
    System.setOut(captureOut);
  }

  // stop capturing System.out
  public void stop() {
    System.out.flush();
    System.setOut(originalOut);
  }

  // access the captured output
  public String getOutput() {
    return baos.toString();
  }

  // clear the captured output
  public void clearOutput() {
    baos.reset();
  }
}

