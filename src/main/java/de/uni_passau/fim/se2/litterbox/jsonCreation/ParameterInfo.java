package de.uni_passau.fim.se2.litterbox.jsonCreation;

import de.uni_passau.fim.se2.litterbox.ast.model.type.BooleanType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.Type;

public class ParameterInfo {
    private String name;
    private String defaultValue;
    private String id;

    public ParameterInfo(String name, String id, Type type) {
        this.name = name;
        this.id = id;
        if (type instanceof BooleanType) {
            defaultValue = "\\\"false\\\"";
        } else {
            defaultValue = "\\\"\\\"";
        }
    }

    public String getName() {
        return name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getId() {
        return id;
    }
}
