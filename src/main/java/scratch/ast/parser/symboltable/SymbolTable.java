/*
 * Copyright (C) 2019 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package scratch.ast.parser.symboltable;

import scratch.ast.model.Message;
import scratch.ast.model.expression.list.ExpressionList;
import scratch.ast.model.type.Type;

import java.util.LinkedHashMap;
import java.util.Map;

public class SymbolTable {

    private LinkedHashMap<String, VariableInfo> variables;
    private LinkedHashMap<String, MessageInfo> messages;
    private LinkedHashMap<String, ExpressionListInfo> lists;

    public SymbolTable() {
        this.variables = new LinkedHashMap<>();
        this.messages = new LinkedHashMap<>();
        this.lists = new LinkedHashMap<>();
    }

    public Map<String, VariableInfo> getVariables() {
        return variables;
    }

    public Map<String, MessageInfo> getMessages() {
        return messages;
    }

    public Map<String, ExpressionListInfo> getLists() {
        return lists;
    }

    public void addVariable(String ident, String variableName, Type type, boolean global, String actorName) {
        VariableInfo info = new VariableInfo(global, actorName, ident, type, variableName);
        variables.put(ident, info);
    }

    public void addExpressionListInfo(String ident, String listName, ExpressionList expressionList, boolean global,
        String actorName) {
        ExpressionListInfo info = new ExpressionListInfo(global, actorName, ident, expressionList, listName);
        lists.put(ident, info);
    }

    public void addMessage(String ident, Message message, boolean global, String actorName) {
        MessageInfo info = new MessageInfo(global, actorName, ident, message);
        messages.put(ident, info);
    }

}
