package edu.duke.ece651.simulationserver;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import edu.duke.ece651.Controller.CommandController;
import edu.duke.ece651.dto.CommandRequest;
import edu.duke.ece651.dto.SessionObject;
import edu.duke.ece651.dto.TextualObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
public class CommandControllerTest {
    @Mock
    private SimulationTextView simulationTextView;

    @InjectMocks
    private CommandController commandController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void test_wsExecuteCommand_success() throws IOException {
        CommandRequest request = new CommandRequest();
        request.setId(1);
        request.setCommand("verbose 2");
        SessionObject result = commandController.wsExecuteCommand(request);
        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    public void test_wsNewBuilding_success() throws Exception {
        SessionObject sessionObject = new SessionObject();
        sessionObject.setId(1);
        Map<String, Object> jsonData = new HashMap<>();
        sessionObject.setJsonData(jsonData);
        
        SessionObject result = commandController.wsNewBuilding(sessionObject);
        assertEquals(1, result.getId());
        assertNotNull(result.getJsonData());
    }

    @Test
    public void test_wsLoadCommand_success() throws Exception {
        SessionObject sessionObject = new SessionObject();
        sessionObject.setId(1);
        sessionObject.setJsonData(new HashMap<>());
        
        SessionObject result = commandController.wsLoadCommand(sessionObject);
        assertEquals(1, result.getId());
        assertNotNull(result.getJsonData());
    }

    @Test
    public void test_wsTextualExecuteCommand_success() throws IOException {
        CommandRequest request = new CommandRequest();
        request.setId(1);
        request.setCommand("verbose 2");
        Map<String, Object> result = commandController.wsTextualExecuteCommand(request);
        assertNotNull(result);
        assertEquals(1, result.get("sessionID"));
    }

    @Test
    public void test_wsTextualNewBuilding() throws Exception {
        TextualObject textualObject = new TextualObject();
        textualObject.setId(1);
        String fileName = "a.json";
        textualObject.setFileName(fileName);
        
        Map<String, Object> result = commandController.wsTextualNewBuilding(textualObject);
        assertEquals(1, result.get("sessionID"));
    }

    @Test
    public void test_wsTextualLoadCommand_success() throws Exception {
        TextualObject textualObject = new TextualObject();
        textualObject.setId(1);
        String fileName = "session1.json";
        textualObject.setFileName(fileName);
        
        Map<String, Object> result = commandController.wsTextualLoadCommand(textualObject);
        assertEquals(1, result.get("sessionID"));
    }
    
}
