
package edu.duke.ece651.simulationserver;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class SimulationTextViewTest {
  @Test
  public void test_parse() throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    SimulationTextView view = new SimulationTextView("src/test/resources/doors1.json", br);

    view.parseCommand("request 'door' from 'D'");
    assertThrows(IllegalArgumentException.class, () -> view.parseCommand("reques 'door' from 'D'"));
    assertThrows(IllegalArgumentException.class, () -> view.parseCommand("request 'door' 'D'"));
    assertThrows(IllegalArgumentException.class, () -> view.parseCommand("request 'door' form 'D'"));
    assertThrows(IllegalArgumentException.class, () -> view.parseCommand("request 'door' from D"));
    assertThrows(IllegalArgumentException.class, () -> view.parseCommand("request door from 'D'"));

    view.parseCommand("step  1");
    assertThrows(IllegalArgumentException.class, () -> view.parseCommand("step 4 steps"));

    view.parseCommand("set policy request 'ready' on 'D'");
    assertThrows(IllegalArgumentException.class, () -> view.parseCommand("set policy request 'ready' 'D'"));
    assertThrows(IllegalArgumentException.class, () -> view.parseCommand("set Policy request 'ready' on 'D'"));
    assertThrows(IllegalArgumentException.class, () -> view.parseCommand("set policy request 'ready' On 'D'"));
    assertThrows(IllegalArgumentException.class, () -> view.parseCommand("set policy request 'ready on 'D'"));
    assertThrows(IllegalArgumentException.class, () -> view.parseCommand("set policy request 'ready' on D'"));
    view.parseCommand("set policy request 'fifo' on default");
    view.parseCommand("set policy request 'fifo' on *");

    // view.parseCommand("verbose 1");
    assertThrows(IllegalArgumentException.class, () -> view.parseCommand("verbose"));
    assertThrows(IllegalArgumentException.class, () -> view.parseCommand("verbose 3"));

    view.parseCommand("set policy source 'simplelat' on 'Hi'");
    assertThrows(IllegalArgumentException.class, () -> view.parseCommand("set policy source 'simplelat' 'Hi'"));
    assertThrows(IllegalArgumentException.class, () -> view.parseCommand("set Policy source 'simplelat' on 'Hi'"));
    assertThrows(IllegalArgumentException.class, () -> view.parseCommand("set policy source 'simplelat' On 'Hi'"));
    assertThrows(IllegalArgumentException.class, () -> view.parseCommand("set policy source 'simplelat on 'Hi'"));
    assertThrows(IllegalArgumentException.class, () -> view.parseCommand("set policy source 'simplelat' on Hi'"));
    view.parseCommand("set policy source 'qlen' on default");
    view.parseCommand("set policy source 'qlen' on *");

    assertThrows(IllegalArgumentException.class, () -> view.parseCommand("set policy Source 'qlen' on *"));

    // view.parseCommand("load src/test/resources/doors1.json");
    assertThrows(IllegalArgumentException.class, () -> view.parseCommand("load"));
    assertThrows(IOException.class, () -> view.parseCommand("load src/test/resources/notExist.json"));

    assertThrows(IllegalArgumentException.class, () -> view.parseCommand("finish run"));
    // view.parseCommand("finish");
    assertThrows(IllegalArgumentException.class,
        () -> view.parseCommand("save"));
  }

  @Test
  public void test_runCatchesIllegalArgument() throws IOException {
    String input = String.join("\n",
        "someinvalid",
        "finish");

    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    BufferedReader br = new BufferedReader(new StringReader(input));
    SimulationTextView view = new SimulationTextView("src/test/resources/doors1.json", br);
    view.run();

    System.setOut(originalOut);
    String consoleOutput = outContent.toString();
    assertTrue(consoleOutput.contains("Invalid command"));
  }

  @Test
  public void test_runWithNoInput() throws IOException {
    String input = "";
    BufferedReader br = new BufferedReader(new StringReader(input));

    SimulationTextView view = new SimulationTextView("src/test/resources/doors1.json", br);
    view.run();
  }

  // @Test
  // public void test() throws IOException {
  //   BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
  //   SimulationTextView view = new SimulationTextView("src/test/resources/doors1.json", br);
  //   ArrayList<String> ans = view.splitCommand("request 'door' from 'door Storage2 (100)'");
  //   ArrayList<String> removeList = new ArrayList<>();
  //   removeList.add("");
  //   ans.removeAll(removeList);
  //   // System.out.println(ans);
  //   assertEquals(4, ans.size());
  // }

  @Test
  public void test_drone() throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    SimulationTextView view = new SimulationTextView("src/test/resources/doors1.json", br);
    view.parseCommand("create src/test/resources/newDronePort.json");
    assertThrows(IllegalArgumentException.class, () -> view.parseCommand("add_drone on 'DP'"));
    view.parseCommand("add_drone at 'DP'");
    view.parseCommand("request 'hinge' from 'Hi'");
    view.parseCommand("step 3");
    view.parseCommand("save save_with_drone_tv.json");
    for (int i = 0; i < 9; i++) {
      view.parseCommand("add_drone at 'DP'");
    }
    assertThrows(IllegalArgumentException.class, () -> view.parseCommand("add_drone at 'DP'"));
    view.parseCommand("finish");
    view.parseCommand("load save_with_drone_tv.json");
    view.parseCommand("add_drone at 'DP'");
  }

  @Test
  public void test_disposal() throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    SimulationTextView view = new SimulationTextView("src/test/resources/doors1.json", br);
    assertThrows(IllegalArgumentException.class, () -> view.parseCommand("create src/test/resources/newDisposal.json"));
  }

  @Test
  public void test_create() throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    SimulationTextView view = new SimulationTextView("src/test/resources/doors1.json", br);
    view.parseCommand("create src/test/resources/newMine.json");
    view.parseCommand("create src/test/resources/newStorage.json");
    view.parseCommand("create src/test/resources/newFactory.json");
    view.parseCommand("connect 'M2' to 'M_S'");
    view.parseCommand("connect 'M_S' to 'Hi2'");
    view.parseCommand("disconnect 'M2' to 'M_S'");
    view.parseCommand("disconnect 'M_S' to 'Hi2'");
    view.parseCommand("remove 'M2'");
    view.parseCommand("remove 'M_S'");
    view.parseCommand("remove 'Hi2'");
  }
}
