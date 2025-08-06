package edu.duke.ece651.dto;

public class CommandRequest {
    private String command;
    private int id;
    public CommandRequest() {}

    public String getCommand() {
        return command;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setCommand(String command) {
        this.command = command;
    }
}

