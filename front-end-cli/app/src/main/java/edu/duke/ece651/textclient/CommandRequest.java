package edu.duke.ece651.textclient;

public class CommandRequest {
    private int id;
    private String command;

    public CommandRequest() {}
    public CommandRequest(int id, String command) {
        this.id = id;
        this.command = command;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }
    
}
