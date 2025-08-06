package edu.duke.ece651.Controller;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.duke.ece651.dto.CommandRequest;
import edu.duke.ece651.dto.SessionObject;
import edu.duke.ece651.dto.TextualObject;
import edu.duke.ece651.simulationserver.OutputCapture;
import edu.duke.ece651.simulationserver.SimulationTextView;
@RestController
@RequestMapping("/command")
@CrossOrigin(origins = "*") // allow cross-origin requests
public class CommandController {

    private final SimulationTextView simulationTextView;

    @Autowired
    public CommandController(SimulationTextView simulationTextView) {
        this.simulationTextView = simulationTextView;
    }

    @MessageMapping("/command")
    @SendTo("/topic/command-result")
    public SessionObject wsExecuteCommand(CommandRequest request) {
        int id = request.getId();
        SessionObject sessionObject = new SessionObject();
        sessionObject.setId(id);

        try {
            String sessionFile = "session" + id + ".json";
            simulationTextView.parseCommand("load " + sessionFile);
            simulationTextView.parseCommand(request.getCommand());
            simulationTextView.parseCommand("save " + sessionFile);

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> jsonMap = objectMapper.readValue(new File(sessionFile), Map.class);

            sessionObject.setJsonData(jsonMap);
            return sessionObject;
        } catch (IOException | IllegalArgumentException e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("status", "error");
            errorMap.put("error", "Invalid Command");
            errorMap.put("details", e.getMessage());
            sessionObject.setJsonData(errorMap);
            return sessionObject;
        }
    }

    @MessageMapping("/newBuilding")
    @SendTo("/topic/newBuilding-result")
    public SessionObject wsNewBuilding(SessionObject sessionObject) {
        int id = sessionObject.getId();
        Map<String, Object> jsonData = sessionObject.getJsonData();
        SessionObject result = new SessionObject();
        result.setId(id);

        try {
            String filename = "newBuilding.json";
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filename), jsonData);

            simulationTextView.parseCommand("create " + filename);
            Files.deleteIfExists(Paths.get(filename));

            String sessionFile = "session" + id + ".json";
            simulationTextView.parseCommand("save " + sessionFile);

            Map<String, Object> updatedData = mapper.readValue(new File(sessionFile), Map.class);
            result.setJsonData(updatedData);
            return result;
        } catch (Exception e) {
            Map<String, Object> errorMap = Map.of(
                "status", "error",
                "message", e.getMessage()
            );
            result.setJsonData(errorMap);
            return result;
        }
    }

    @MessageMapping("/loadCommand")
    @SendTo("/topic/loadCommand-result")
    public SessionObject wsLoadCommand(SessionObject sessionObject) {
        int id = sessionObject.getId();
        Map<String, Object> jsonData = sessionObject.getJsonData();
        SessionObject result = new SessionObject();
        result.setId(id);

        try {
            String filename = "loadJsonFile.json";
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filename), jsonData);

            simulationTextView.parseCommand("load " + filename);
            Files.deleteIfExists(Paths.get(filename));

            String sessionFile = "session" + id + ".json";
            simulationTextView.parseCommand("save " + sessionFile);

            Map<String, Object> updatedData = mapper.readValue(new File(sessionFile), Map.class);
            result.setJsonData(updatedData);
            return result;
        } catch (Exception e) {
            Map<String, Object> errorMap = Map.of(
                "status", "error",
                "message", e.getMessage()
            );
            result.setJsonData(errorMap);
            return result;
        }
    }

    @MessageMapping("/loadSession")
    @SendTo("/topic/loadSession-result")
    public SessionObject wsLoadSession(int id) {
        SessionObject result = new SessionObject();
        result.setId(id);

        try {
            ObjectMapper mapper = new ObjectMapper();
            String sessionFile = "session" + id + ".json";
            Map<String, Object> jsonMap = mapper.readValue(new File(sessionFile), Map.class);

            result.setJsonData(jsonMap);
            return result;
        } catch (Exception e) {
            Map<String, Object> errorMap = Map.of(
                "status", "error",
                "message", e.getMessage()
            );
            result.setJsonData(errorMap);
            return result;
        }
    }

    @MessageMapping("/newSession")
    @SendTo("/topic/newSession-result")
    public SessionObject wsNewSession(int id) {
        SessionObject result = new SessionObject();
        result.setId(id);

        String sessionFile = "session" + id + ".json";
        File file = new File(sessionFile);

        try {
            if (file.exists()) {
                Map<String, Object> errorMap = Map.of(
                    "status", "error",
                    "message", "Session already exists"
                );
                result.setJsonData(errorMap);
                return result;
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(file, new HashMap<>());

            Map<String, Object> successMap = Map.of(
                "status", "ok",
                "message", "Blank session file created"
            );
            result.setJsonData(successMap);
            return result;

        } catch (IOException e) {
            Map<String, Object> errorMap = Map.of(
                "status", "error",
                "message", "Failed to create session file",
                "details", e.getMessage()
            );
            result.setJsonData(errorMap);
            return result;
        }
    }



    
    // textual command endpoint
    @MessageMapping("/textual/command")
    @SendTo("/topic/textual/command-result")
    public Map<String, Object> wsTextualExecuteCommand(CommandRequest request) {
        int id = request.getId();
        try {
            Map<String, Object> response = new HashMap<>();
            OutputCapture capture = new OutputCapture();
            //load session file
            String sessionFile = "session" + id + ".json";
            String loadCommand = "load"+" "+sessionFile;
            simulationTextView.parseCommand(loadCommand);
            // execute command logic
            capture.clearOutput();
            capture.start();
            simulationTextView.parseCommand(request.getCommand());
            capture.stop();
            String output = capture.getOutput();
            capture.clearOutput();
            // session file
            
            // save session file
            String command = "save"+" "+sessionFile;
            simulationTextView.parseCommand(command);

            // return output
            response.put("sessionID", id);
            response.put("status", "ok");
            response.put("output", output);
            return response;
        } catch (IOException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("sessionID", id);
            response.put("status", "error");
            response.put("error", "Invalid Command");
            response.put("details", e.getMessage());
            // handle exception
            return response;
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("sessionID", id);
            response.put("status", "error");
            response.put("error", "Invalid Command");
            response.put("details", e.getMessage());
            // deal with invalid command exception
            return response;
        }
    }

    // textual WebSocket endpoint for new building creation
    @MessageMapping("/textual/newBuilding")
    @SendTo("/topic/textual/newBuilding-result")
    public Map<String, Object> wsTextualNewBuilding(TextualObject textualObject) {
        int id = textualObject.getId();
        try {
            String filename = textualObject.getFileName();
            Map<String, Object> response = new HashMap<>();
            OutputCapture capture = new OutputCapture();
    
            // execute command logic
            capture.clearOutput();
            capture.start();
            simulationTextView.parseCommand("create " + filename);
            capture.stop();
            String output = capture.getOutput();
            capture.clearOutput();

            //session file
            String sessionFile = "session" + id + ".json";
            // save
            String command = "save"+" "+sessionFile;
            simulationTextView.parseCommand(command);

            // return output
            response.put("sessionID", id);
            response.put("status", "ok");
            response.put("output", output);
            return response;
        } catch (Exception e) {
            return Map.of("sessionID", id,"status", "error", "message", e.getMessage());
        }
    }

    // textual WebSocket endpoint for new building creation
    @MessageMapping("/textual/loadCommand")
    @SendTo("/topic/textual/loadCommand-result")
    public Map<String, Object> wsTextualLoadCommand(TextualObject textualObject) {
        int id = textualObject.getId();
        try {
            String filename = textualObject.getFileName();
            Map<String, Object> response = new HashMap<>();
            OutputCapture capture = new OutputCapture();
    
            // execute command logic
            capture.clearOutput();
            capture.start();
            simulationTextView.parseCommand("load " + filename);
            capture.stop();
            String output = capture.getOutput();
            capture.clearOutput();

            //session file
            String sessionFile = "session" + id + ".json";
            // save
            String command = "save"+" "+sessionFile;
            simulationTextView.parseCommand(command);

            // return output
            response.put("sessionID", id);
            response.put("status", "ok");
            response.put("output", output);
            return response;
        } catch (Exception e) {
            return Map.of("sessionID", id,"status", "error", "message", e.getMessage());
        }
    }

    // textual WebSocket endpoint for new building creation
    @MessageMapping("/textual/loadSession")
    @SendTo("/topic/textual/loadSession-result")
    public Map<String, Object> wsTextualLoadSession(int id) {
        // load session file
        String sessionFile = "session" + id + ".json";
        try {
            Map<String, Object> response = new HashMap<>();
            OutputCapture capture = new OutputCapture();
    
            // execute command logic
            capture.clearOutput();
            capture.start();
            simulationTextView.parseCommand("load " + sessionFile);
            capture.stop();
            String output = capture.getOutput();
            capture.clearOutput();

            // return output
            response.put("sessionID", id);
            response.put("status", "ok");
            response.put("output", output);
            return response;
        } catch (Exception e) {
            return Map.of("sessionID", id,"status", "error", "message", e.getMessage());
        }
    }

    // textual WebSocket endpoint for new building creation
    @MessageMapping("/textual/newSession")
    @SendTo("/topic/textual/newSession-result")
    public Map<String, Object> wsTextualNewSession(int id) {
        String sessionFile = "session" + id + ".json";
        File file = new File(sessionFile);
        // if the file already exists, return an error message
        if (file.exists()) {
            return Map.of(
                "sessionID", id,
                "status", "error",
                "message", "Session already exists"
            );
        }
        try {
            // write an empty JSON object to the file
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(file, new HashMap<>());
            // return success message
            return Map.of(
                "sessionID", id,
                "status", "ok",
                "message", "Blank session file created"
            );
    
        } catch (IOException e) {
            return Map.of(
                "sessionID", id,
                "status", "error",
                "message", "Failed to create session file",
                "details", e.getMessage()
            );
        }
    }
}
