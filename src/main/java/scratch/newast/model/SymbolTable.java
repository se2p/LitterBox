package scratch.newast.model;

import java.util.HashMap;
import scratch.newast.model.expression.list.ExpressionList;
import scratch.newast.model.variable.Variable;

public class SymbolTable {

    public static HashMap<String, SymbolTable> TABLES = new HashMap<>();

    private HashMap<String, Variable> variables;
    private HashMap<String, Message> messages;
    private HashMap<String, ExpressionList> lists;

    public SymbolTable(HashMap<String, Variable> variables,
        HashMap<String, Message> messages,
        HashMap<String, ExpressionList> lists) {

        this.variables = variables;
        this.messages = messages;
        this.lists = lists;
    }

    public HashMap<String, Variable> getVariables() {
        return variables;
    }

    public HashMap<String, Message> getMessages() {
        return messages;
    }

    public HashMap<String, ExpressionList> getLists() {
        return lists;
    }
}
