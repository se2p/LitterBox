package scratch.newast.parser.symboltable;

import java.util.HashMap;
import scratch.newast.model.Message;
import scratch.newast.model.expression.list.ExpressionList;
import scratch.newast.model.type.Type;
import scratch.newast.model.variable.Variable;

public class SymbolTable {

    private HashMap<String, VariableInfo> variables;
    private HashMap<String, MessageInfo> messages;
    private HashMap<String, ExpressionListInfo> lists;

    public SymbolTable() {
        this.variables = new HashMap<>();
        this.messages = new HashMap<>();
        this.lists = new HashMap<>();
    }

    public HashMap<String, VariableInfo> getVariables() {
        return variables;
    }

    public HashMap<String, MessageInfo> getMessages() {
        return messages;
    }

    public HashMap<String, ExpressionListInfo> getLists() {
        return lists;
    }

    public void addVariable(String ident, Type type, boolean global, String scriptGroupName) {
        VariableInfo info = new VariableInfo(global, scriptGroupName, ident, type);
        variables.put(ident, info);
    }

    public void addExpressionListInfo(String ident, ExpressionList expressionList, boolean global,
        String scriptGroupName) {
        ExpressionListInfo info = new ExpressionListInfo(global, scriptGroupName, ident, expressionList);
        lists.put(ident, info);
    }

    public void addMessage(String ident, Message message, boolean global, String scriptGroupName) {
        MessageInfo info = new MessageInfo(global, scriptGroupName, ident, message);
        messages.put(ident, info);
    }

}
