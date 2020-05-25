package de.uni_passau.fim.se2.litterbox.jsonCreation;

public class IdJsonStringTuple {
    private String id;
    private String jsonString;

    public IdJsonStringTuple(String id, String jsonString) {
        this.id = id;
        this.jsonString = jsonString;
    }

    public String getId() {
        return id;
    }

    public String getJsonString() {
        return jsonString;
    }
}
