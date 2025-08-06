package edu.duke.ece651.dto;

import java.util.Map;

public class SessionObject {
    private int id;
    private Map<String, Object> jsonData;

    public SessionObject() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Map<String, Object> getJsonData() {
        return jsonData;
    }

    public void setJsonData(Map<String, Object> jsonData) {
        this.jsonData = jsonData;
    }
}
