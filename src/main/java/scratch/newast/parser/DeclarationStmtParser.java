package scratch.newast.parser;

import static scratch.newast.Constants.DECLARATION_LIST_NAME_POS;
import static scratch.newast.Constants.DECLARATION_LIST_VALUES_POS;
import static scratch.newast.Constants.DECLARATION_VARIABLE_NAME_POS;
import static scratch.newast.Constants.DECLARATION_VARIABLE_VALUE_POS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import scratch.newast.model.DeclarationStmt;
import scratch.newast.model.Message;
import scratch.newast.model.expression.Expression;
import scratch.newast.model.expression.bool.Bool;
import scratch.newast.model.expression.list.ExpressionList;
import scratch.newast.model.expression.list.ExpressionListPlain;
import scratch.newast.model.expression.num.Number;
import scratch.newast.model.expression.string.Str;
import scratch.newast.model.statement.common.SetStmt;
import scratch.newast.model.statement.common.SetVariableTo;
import scratch.newast.model.type.BooleanType;
import scratch.newast.model.type.ListType;
import scratch.newast.model.type.NumberType;
import scratch.newast.model.type.StringType;
import scratch.newast.model.variable.Identifier;
import scratch.newast.model.variable.Qualified;

public class DeclarationStmtParser {

    public static List<DeclarationStmt> parseVariables(JsonNode variableNode, String scriptGroupName, boolean isStage) {
        Preconditions.checkNotNull(variableNode);
        List<DeclarationStmt> parsedVariables = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> iter = variableNode.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> currentEntry = iter.next();
            Preconditions.checkArgument(currentEntry.getValue().isArray());
            ArrayNode arrNode = (ArrayNode) currentEntry.getValue();
            if (arrNode.get(DECLARATION_VARIABLE_VALUE_POS).isNumber()) {
                ProgramParser.symbolTable.addVariable(arrNode.get(DECLARATION_VARIABLE_NAME_POS).textValue(),
                    new NumberType(), isStage, scriptGroupName);
                parsedVariables.add(
                    new DeclarationStmt(new Identifier(arrNode.get(DECLARATION_VARIABLE_NAME_POS).textValue()),
                        new NumberType()));
            } else if (arrNode.get(DECLARATION_VARIABLE_VALUE_POS).isBoolean()) {
                ProgramParser.symbolTable.addVariable(arrNode.get(DECLARATION_VARIABLE_NAME_POS).textValue(),
                    new BooleanType(), isStage, scriptGroupName);
                parsedVariables.add(
                    new DeclarationStmt(new Identifier(arrNode.get(DECLARATION_VARIABLE_NAME_POS).textValue()),
                        new BooleanType()));
            } else {
                ProgramParser.symbolTable.addVariable(arrNode.get(DECLARATION_VARIABLE_NAME_POS).textValue(),
                    new StringType(), isStage, scriptGroupName);
                parsedVariables.add(
                    new DeclarationStmt(new Identifier(arrNode.get(DECLARATION_VARIABLE_NAME_POS).textValue()),
                        new StringType()));
            }
        }
        return parsedVariables;
    }

    public static List<SetStmt> parseVariableSetStmts(JsonNode variableNode, String scriptGroupName) {
        Preconditions.checkNotNull(variableNode);
        List<SetStmt> parsedVariables = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> iter = variableNode.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> currentEntry = iter.next();
            Preconditions.checkArgument(currentEntry.getValue().isArray());
            ArrayNode arrNode = (ArrayNode) currentEntry.getValue();
            //TODO check is ExpressionParser should be used
            if (arrNode.get(DECLARATION_VARIABLE_VALUE_POS).isNumber()) {
                parsedVariables.add(new SetVariableTo(new Qualified(new Identifier(scriptGroupName),
                    new Identifier(arrNode.get(DECLARATION_VARIABLE_NAME_POS).textValue())),
                    new Number((float) arrNode.get(DECLARATION_VARIABLE_VALUE_POS).asDouble())));
            } else if (arrNode.get(DECLARATION_VARIABLE_VALUE_POS).isBoolean()) {
                parsedVariables.add(new SetVariableTo(new Qualified(new Identifier(scriptGroupName),
                    new Identifier(arrNode.get(DECLARATION_VARIABLE_NAME_POS).textValue())),
                    new Bool(arrNode.get(DECLARATION_VARIABLE_VALUE_POS).asBoolean())));
            } else {
                parsedVariables.add(new SetVariableTo(new Qualified(new Identifier(scriptGroupName),
                    new Identifier(arrNode.get(DECLARATION_VARIABLE_NAME_POS).textValue())),
                    new Str(arrNode.get(DECLARATION_VARIABLE_VALUE_POS).textValue())));
            }
        }
        return parsedVariables;
    }

    public static List<DeclarationStmt> parseLists(JsonNode listsNode, String scriptGroupName, boolean isStage) {
        Preconditions.checkNotNull(listsNode);
        List<DeclarationStmt> parsedLists = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> iter = listsNode.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> currentEntry = iter.next();
            Preconditions.checkArgument(currentEntry.getValue().isArray());
            ArrayNode arrNode = (ArrayNode) currentEntry.getValue();
            String listName = arrNode.get(DECLARATION_LIST_NAME_POS).textValue();
            JsonNode listValues = arrNode.get(DECLARATION_LIST_VALUES_POS);
            Preconditions.checkArgument(listValues.isArray());
            ExpressionList expressionList = new ExpressionList(makeExpressionListPlain((ArrayNode) listValues));
            ProgramParser.symbolTable.addExpressionListInfo(listName, expressionList, isStage, scriptGroupName);
            parsedLists.add(new DeclarationStmt(new Identifier(listName), new ListType()));
        }
        return parsedLists;
    }

    private static ExpressionListPlain makeExpressionListPlain(ArrayNode valuesArray) {
        List<Expression> expressions = new ArrayList<>();
        for (int i = 0; i < valuesArray.size(); i++) {
            //TODO  check if expressionParser should be used
            expressions.add(new Str(valuesArray.get(i).textValue()));
        }
        return new ExpressionListPlain(expressions);
    }

    public static List<SetStmt> parseListSetStmts(JsonNode listNode, String scriptGroupName) {
        Preconditions.checkNotNull(listNode);
        List<SetStmt> parsedLists = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> iter = listNode.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> currentEntry = iter.next();
            Preconditions.checkArgument(currentEntry.getValue().isArray());
            ArrayNode arrNode = (ArrayNode) currentEntry.getValue();
            String listName = arrNode.get(DECLARATION_LIST_NAME_POS).textValue();
            JsonNode listValues = arrNode.get(DECLARATION_LIST_VALUES_POS);
            Preconditions.checkState(listValues.isArray(), "List Values is expected to be an array but is %s",
                listValues.getNodeType().toString());
            parsedLists.add(new SetVariableTo(new Qualified(new Identifier(scriptGroupName), new Identifier(listName)),
                makeExpressionListPlain((ArrayNode) listValues)));
        }
        return parsedLists;
    }

    public static List<DeclarationStmt> parseBroadcasts(JsonNode broadcastsNode, String scriptGroupName,
        boolean isStage) {
        Preconditions.checkNotNull(broadcastsNode);
        List<DeclarationStmt> parsedBroadcasts = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> iter = broadcastsNode.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> current = iter.next();
            ProgramParser.symbolTable.addMessage(current.getValue().textValue(),
                new Message(current.getValue().textValue()), isStage, scriptGroupName);
            parsedBroadcasts.add(new DeclarationStmt(new Identifier(current.getValue().textValue()), new StringType()));
        }
        return parsedBroadcasts;
    }
}
