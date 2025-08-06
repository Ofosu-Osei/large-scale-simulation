package edu.duke.ece651.simulationserver;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The SimulationTextView class provides a text‐based user interface for the simulation.
 * 
 * It reads commands from a provided BufferedReader (which may be System.in or a custom input stream),
 * parses these commands, and delegates them to the underlying Simulation object. Supported commands include
 * production requests, simulation stepping, finishing the simulation, setting verbosity, saving and loading state,
 * and setting selection policies.
 * 
 */
@Component
public class SimulationTextView {
  private Simulation simulation;
  BufferedReader bufferedReader;
  Boolean finished;

  /**
   * Constructs a new SimulationTextView by initializing a Simulation from the given JSON configuration
   * file and using the provided BufferedReader as the command input source.
   *
   * @param fileName the path to the JSON configuration file used to initialize the simulation
   * @param br a BufferedReader from which user commands will be read
   * @throws IOException if an error occurs during simulation initialization
   */
  public SimulationTextView(String fileName, BufferedReader br) throws IOException {
    simulation = new Simulation(fileName);
    bufferedReader = br;
    finished = false;
  }

  @Autowired
  public SimulationTextView(@Value("${simulation.config-file}")String fileName) throws IOException {
    simulation = new Simulation(fileName);
    bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    finished = false;
  }


  /**
   * Runs the interactive command loop.
   * 
   * This method reads lines from the bufferedReader and passes them to parseCommand
   * for processing. If a command sets the finished flag (for example, via the "finish" command),
   * the loop terminates. 
   *
   * @throws IOException if an error occurs during reading from the bufferedReader
   */
  public void run() throws IOException {
    System.out.print(Simulation.getCycle() + "> ");
    String line = bufferedReader.readLine();
    while (line != null) {
      try {
        parseCommand(line);
        if (finished) {
          return;
        }
      }
      catch (IllegalArgumentException e) {
        System.out.println(e.getMessage());
      }
      catch (FileNotFoundException e) {
        System.out.println(e.getMessage());
      }
      finally {
        if (!finished) {
          System.out.print(Simulation.getCycle() + "> ");
          line = bufferedReader.readLine();
        }
      }
    }
  }

  /**
   * Parses and executes a single command.
   * 
   * The command is tokenized by splitting on spaces and then processed based on the first token.
   * Supported command types include: "request", "step", "finish", "verbose", "save", "load", and "set".
   *
   * @param input the full command string entered by the user
   * @throws IOException if an error occurs during command processing (e.g., while saving or loading)
   * @throws IllegalArgumentException if the command is not recognized or is improperly formatted
   */
  public void parseCommand(String input) throws IOException {
    ArrayList<String> tokens = splitCommand(input);
    ArrayList<String> removeList = new ArrayList<>();
    removeList.add("");
    tokens.removeAll(removeList);
    String commandType = tokens.get(0);
    switch (commandType) {
      case "request":
        parseRequestCommand(tokens);
        break;
      case "step":
        parseStepCommand(tokens);
        break;
      case "finish":
        parseFinishCommand(tokens);
        break;
      case "verbose":
        parseVerboseCommand(tokens);
        break;
      case "save":
        parseSaveCommand(tokens);
        break;
      case "load":
        parseLoadCommand(tokens);
        break;
      case "set":
        parseSetPolicyCommand(tokens);
        break;
      case "connect":
        parseConnectCommand(tokens);
        break;
      case "create":
        parseCreateCommand(tokens);
        break;
      case "disconnect":
        parseDisconnectCommand(tokens);
        break;
      case "add_drone":
        parseAddDrone(tokens);
        break;
      case "remove":
        parseRemoveCommand(tokens);
        break;
    default:
        throw new IllegalArgumentException("Invalid command4");
    }
  }

  private ArrayList<String> splitCommand(String command) {
    ArrayList<String> ans = new ArrayList<>();
    int startIdx = 0;
    while (true) {
      int startQuoteIdx;
      int endQuoteIdx;
      int idx1 = command.indexOf('\'', startIdx);
      if (idx1 == -1) {
        ans.addAll(new ArrayList<>(Arrays.asList(command.substring(startIdx).split(" "))));
        return ans;
      }
      else {
        if (idx1 != 0 && command.charAt(idx1 - 1) == ' ') {
          startQuoteIdx = idx1;
        }
        else {
          throw new IllegalArgumentException("Invalid command");
        }
      } 
      int idx2 = command.indexOf('\'', startQuoteIdx + 1);
      if (idx2 == -1) {
        throw new IllegalArgumentException("Invalid command");
      }
      else {
        if (idx2 == command.length() - 1 || command.charAt(idx2 + 1) == ' ') {
          endQuoteIdx = idx2;
        }
        else {
          throw new IllegalArgumentException("Invalid command");
        }
      }
      ans.addAll(new ArrayList<>(Arrays.asList(command.substring(startIdx, startQuoteIdx).split(" "))));
      ans.add(command.substring(startQuoteIdx, endQuoteIdx + 1));
      startIdx = endQuoteIdx + 1;
    }
  }

  /**
   * Checks whether the given string is enclosed in single quotes.
   *
   * @param str the string to check
   * @return true if the string has length at least 2 and starts and ends with a single quote or false otherwise
   */
  private Boolean quoted(String str) {
    return str.length() >= 2 && str.startsWith("'") && str.endsWith("'");
  }

  /**
   * Removes the enclosing single quotes from a string.
   *
   * @param str the quoted string
   * @return the string without its first and last characters
   */
  private String unquote(String str) {
    return str.substring(1, str.length() - 1);
  }

  /**
   * Parses and executes a "request" command.
   *
   * @param tokens the tokenized command string
   * @throws IllegalArgumentException if the command is not in the expected format
   */
  private void parseRequestCommand(ArrayList<String> tokens) {
    if (tokens.size() != 4) {
      throw new IllegalArgumentException("Invalid command");
    }
    if (!tokens.get(2).equals("from")) {
      throw new IllegalArgumentException("Invalid command");
    }
    String outputString = tokens.get(1);
    String buildingString = tokens.get(3);
    if (!(quoted(outputString) && quoted(buildingString))) {
      throw new IllegalArgumentException("Invalid command");
    }
    outputString = unquote(outputString);
    buildingString = unquote(buildingString);
    simulation.request(buildingString, outputString);
  }

  /**
   * Parses and executes a "step" command.
   *
   * @param tokens the tokenized command string
   * @throws IllegalArgumentException if the command does not have exactly 2 tokens
   */
  private void parseStepCommand(ArrayList<String> tokens) {
    if (tokens.size() != 2) {
      throw new IllegalArgumentException("Invalid command");
    }
    simulation.stepN(Integer.valueOf(tokens.get(1)));
  }

  /**
   * Parses and executes a "finish" command.
   * 
   * The expected format is simply: {@code finish}. This command instructs the simulation to continue
   * processing until all requests have been completed. Once finished, the finished flag is set to true.
   * 
   *
   * @param tokens the tokenized command string
   * @throws IllegalArgumentException if the command does not have exactly one token
   */
  private void parseFinishCommand(ArrayList<String> tokens) {
    if (tokens.size() != 1) {
      throw new IllegalArgumentException("Invalid command");
    }
    simulation.finish();
    finished = true;
  }

  /**
   * Parses and executes a "set" command to change a policy.
   * 
   * @param tokens the tokenized command string
   * @throws IllegalArgumentException if the command format is invalid
   */
  private void parseSetPolicyCommand(ArrayList<String> tokens) {
    // System.out.println("set policy command tokens: " + tokens);
    if (tokens.size() != 6) {
      throw new IllegalArgumentException("Invalid command");
    }
    if (!(tokens.get(1).equals("policy") && tokens.get(4).equals("on"))) {
      throw new IllegalArgumentException("Invalid command");
    }
    String policyType = tokens.get(2);
    String policyName = tokens.get(3);
    String buildingName = tokens.get(5);
    if (!quoted(policyName)) {
      throw new IllegalArgumentException("Invalid command");
    }
    policyName = unquote(policyName);
    if (policyType.equals("request")) {
      if (buildingName.equals("*")) {
        simulation.setRequestAll(policyName);
      }
      else if (buildingName.equals("default")) {
        simulation.setRequestDefault(policyName);
      }
      else {
        if (!quoted(buildingName)) {
          throw new IllegalArgumentException("Invalid command");
        }
        buildingName = unquote(buildingName);
        simulation.setRequestPolicy(buildingName, policyName);
      }
    }
    else if (policyType.equals("source")) {
      if (buildingName.equals("*")) {
        simulation.setSourceAll(policyName);
      }
      else if (buildingName.equals("default")) {
        simulation.setSourceDefault(policyName);
      }
      else {
        if (!quoted(buildingName)) {
          throw new IllegalArgumentException("Invalid command");
        }
        buildingName = unquote(buildingName);
        simulation.setSourcePolicy(buildingName, policyName);
      }
    }
    else {
      throw new IllegalArgumentException("Invalid command");
    }
  }

  /**
   * Parses and executes a "verbose" command.
   *
   * @param tokens the tokenized command string
   * @throws IllegalArgumentException if the command does not have exactly 2 tokens or if the verbosity level is invalid
   */
  private void parseVerboseCommand(ArrayList<String> tokens) {
    if (tokens.size() != 2) {
      throw new IllegalArgumentException("Invalid command");
    }
    int verbosity = Integer.valueOf(tokens.get(1));
    if (verbosity < 0 || verbosity > 2) {
      throw new IllegalArgumentException("Invalid command");
    }
    simulation.setVerbosity(verbosity);
  }

  /**
   * Parses and executes a "save" command.
   *
   * @param tokens the tokenized command string
   * @throws IOException if an error occurs while saving the simulation state
   * @throws IllegalArgumentException if the command does not have exactly 2 tokens
   */
  private void parseSaveCommand(ArrayList<String> tokens) throws IOException {
    if (tokens.size() != 2) {
      throw new IllegalArgumentException("Invalid command");
    }
    String fileName = tokens.get(1);
    simulation.save(fileName);
  }

  /**
   * Parses and executes a "load" command.
   *
   * @param tokens the tokenized command string
   * @throws IOException if an error occurs while loading the new configuration
   * @throws IllegalArgumentException if the command does not have exactly 2 tokens
   */
  private void parseLoadCommand(ArrayList<String> tokens) throws IOException {
    if (tokens.size() != 2) {
      throw new IllegalArgumentException("Invalid command");
    }
    String fileName = tokens.get(1);
    simulation = new Simulation(fileName);
  }

   /**
   * Parses and executes a "connect" command.
   *
   * @param tokens the tokenized command string
   * @throws IOException if an error occurs while loading the new configuration
   * @throws IllegalArgumentException if the command does not have exactly 4 tokens
   */
  private void parseConnectCommand(ArrayList<String> tokens) {
    if (tokens.size() != 4 || !tokens.get(2).equals("to")) {
      throw new IllegalArgumentException("Invalid command1");
    }
    simulation.connectTwoBuilding(unquote(tokens.get(1)), unquote(tokens.get(3)));
  }

   /**
   * Parses and executes a "disconnect" command.
   *
   * @param tokens the tokenized command string
   * @throws IOException if an error occurs while loading the new configuration
   * @throws IllegalArgumentException if the command does not have exactly 4 tokens
   */
 
  private void parseDisconnectCommand(ArrayList<String> tokens) {
    if (tokens.size() != 4 || !tokens.get(2).equals("to")) {
      throw new IllegalArgumentException("Invalid command1");
    }
    simulation.disconnect(unquote(tokens.get(1)), unquote(tokens.get(3)));
  }
  
  /**
   * Parses and executes a "creeate" command.
   *
   * @param tokens the tokenized command string
   * @throws IOException if an error occurs while loading the new configuration
   * @throws IllegalArgumentException if the command does not have exactly 2 tokens
   */
  private void parseCreateCommand(ArrayList<String> tokens) throws IOException {
    if (tokens.size() != 2) {
      throw new IllegalArgumentException("Invalid command");
    }
    String fileName = tokens.get(1);
    simulation.createBuilding(fileName);
  }
  
  /**
   * Parses and executes a "add_drone" command.
   *
   * @param tokens the tokenized command string
   * @throws IllegalArgumentException if the command is not in the expected format
   */
  private void parseAddDrone(ArrayList<String> tokens) throws IOException {
    if (tokens.size() != 3) {
      throw new IllegalArgumentException("Invalid command: argc");
    }
    if (!tokens.get(1).equals("at")) {
      throw new IllegalArgumentException("Invalid command: at");
    }
    String dronePortName = tokens.get(2);
    if (!quoted(dronePortName)) {
      throw new IllegalArgumentException("Invalid command: no quote");
    }
    simulation.addDrone(unquote(dronePortName));
  }

  /**
   * Parses and executes a "remove" command.
   *
   * @param tokens the tokenized command string
   * @throws IOException if an error occurs while loading the new configuration
   * @throws IllegalArgumentException if the command does not have exactly 2 tokens
   */
  private void parseRemoveCommand(ArrayList<String> tokens) throws IOException {
    if (tokens.size() != 2) {
      throw new IllegalArgumentException("Invalid command3");
    }
    String buildingName = tokens.get(1);
    if (!quoted(buildingName)) {
      throw new IllegalArgumentException("Invalid command");
    }
    simulation.tryRemoveBuilding(unquote(buildingName));
  }
  
}
