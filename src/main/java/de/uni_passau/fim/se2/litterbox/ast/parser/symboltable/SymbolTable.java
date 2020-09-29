/*
 * Copyright (C) 2020 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.parser.symboltable;

import de.uni_passau.fim.se2.litterbox.ast.model.Message;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.type.Type;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

public class SymbolTable {

    private LinkedHashMap<String, VariableInfo> variables;
    private LinkedHashMap<String, MessageInfo> messages;
    private LinkedHashMap<String, ExpressionListInfo> lists;

    /**
     * The symbol table collects all information about variable, lists and messages.
     *
     * <p>This class is primarily used to reference variables, lists and messages, such that we know which
     * actors defined which of these objects and whether they are local or global.</p>
     */
    public SymbolTable() {
        this.variables = new LinkedHashMap<>();
        this.messages = new LinkedHashMap<>();
        this.lists = new LinkedHashMap<>();
    }

    public SymbolTable(SymbolTable other) {
        this.variables = new LinkedHashMap<>(other.variables);
        this.messages = new LinkedHashMap<>(other.messages);
        this.lists = new LinkedHashMap<>(other.lists);
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

    /**
     * Adds a variable to the symbol table.
     *
     * <p>The key for this variable will be ident+actorName because multiple
     * actors can have variables with the same id.</p>
     *
     * @param ident        of the variable
     * @param variableName for the variable
     * @param type         of the variable
     * @param global       indicates whether this variable is global and accessible for all actors
     * @param actorName    name of the actor where the variable is defined
     */
    public void addVariable(String ident, String variableName, Type type, boolean global, String actorName) {
        VariableInfo info = new VariableInfo(global, actorName, ident, type, variableName);
        variables.put(ident + variableName + actorName, info);
    }

    /**
     * Adds a list to the symbol table.
     *
     * <p>The key for this list will be ident+actorName because multiple actors can have variables with the same id.</p>
     *
     * @param ident          of the list
     * @param listName       for the list
     * @param expressionList itself
     * @param global         indicates whether this variable is global and accessible for all actors
     * @param actorName      name of the actor where the variable is defined
     */
    public void addExpressionListInfo(String ident, String listName, ExpressionList expressionList, boolean global,
                                      String actorName) {
        ExpressionListInfo info = new ExpressionListInfo(global, actorName, ident, expressionList, listName);
        lists.put(ident + listName + actorName, info);
    }

    public void addMessage(String name, Message message, boolean global, String actorName, String identifier) {
        MessageInfo info = new MessageInfo(global, actorName, identifier, message);
        messages.put(name, info);
    }

    /**
     * Retrieves a variable based on the identifier and the actorName.
     *
     * <p>If the given actor does not define a given variable </p>
     *
     * <p>The key in the symbol table for variables is the ident + actorName.
     * This is necessary because identifiers of variables are not unique and multiple actors can have variables
     * with the same identifier</p>
     *
     * @param ident     of the variable
     * @param actorName which defines the variable
     * @return
     */
    public Optional<VariableInfo> getVariable(String ident, String variableName, String actorName) {
        String key = ident + variableName + actorName;
        if (variables.containsKey(key)) {
            return Optional.of(variables.get(key));
        }
        String stageKey = ident + variableName + "Stage";
        if (variables.containsKey(stageKey)) {
            return Optional.of(variables.get(stageKey));
        }

        return Optional.empty();
    }

    /**
     * Retrieves a list based on the identifier and the actorName.
     *
     * <p>If the given actor does not define a given list</p>
     *
     * <p>The key in the symbol table for list is the ident + actorName.
     * This is necessary because identifiers of list are not unique and multiple actors can have variables
     * with the same identifier</p>
     *
     * @param ident     of the list
     * @param actorName which defines the variable
     * @return
     */
    public Optional<ExpressionListInfo> getList(String ident, String listName, String actorName) {
        String key = ident + listName + actorName;
        if (lists.containsKey(key)) {
            return Optional.of(lists.get(key));
        }
        String stageKey = ident + listName + "Stage";
        if (lists.containsKey(stageKey)) {
            return Optional.of(lists.get(stageKey));
        }

        return Optional.empty();
    }

    public Optional<MessageInfo> getMessage(String name) {
        if (messages.containsKey(name)) {
            return Optional.of(messages.get(name));
        }

        return Optional.empty();
    }

    public String getListIdentifierFromActorAndName(String actor, String name) {
        Set<Entry<String, ExpressionListInfo>> entries = lists.entrySet();
        for (Entry<String, ExpressionListInfo> current : entries) {
            ExpressionListInfo info = current.getValue();
            if (info.getVariableName().equals(name) && info.getActor().equals(actor)) {
                return current.getValue().getIdent();
            }
        }
        return null;
    }

    public String getVariableIdentifierFromActorAndName(String actor, String name) {
        Set<Entry<String, VariableInfo>> entries = variables.entrySet();
        for (Entry<String, VariableInfo> current : entries) {
            VariableInfo info = current.getValue();
            if (info.getVariableName().equals(name) && info.getActor().equals(actor)) {
                return current.getValue().getIdent();
            }
        }
        return null;
    }
}
