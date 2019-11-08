package scratch.newast.parser.symboltable;

import scratch.newast.model.Message;

public class MessageInfo {

    boolean global;
    String scriptGroup;
    String variableName;
    Message message;

    public MessageInfo(boolean global, String scriptGroup, String ident,
        Message message) {
        this.global = global;
        this.scriptGroup = scriptGroup;
        this.variableName = ident;
        this.message = message;
    }

    public boolean isGlobal() {
        return global;
    }

    public String getScriptGroup() {
        return scriptGroup;
    }

    public String getVariableName() {
        return variableName;
    }

    public Message getMessage() {
        return message;
    }
}
