package scratch.newast.parser.symboltable;

import scratch.newast.model.type.Type;

public class ArgumentInfo {
    String name;
    Type type;

    public ArgumentInfo(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }
}
