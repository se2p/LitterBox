package scratch.newast.parser;

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
import scratch.newast.model.expression.list.ExpressionList;
import scratch.newast.model.expression.list.ExpressionListPlain;
import scratch.newast.model.expression.string.Str;
import scratch.newast.model.type.BooleanType;
import scratch.newast.model.type.ListType;
import scratch.newast.model.type.NumberType;
import scratch.newast.model.type.StringType;
import scratch.newast.model.variable.Identifier;

public class DeclarationParser {

    public static List<DeclarationStmt> parseVariables(JsonNode variableNode, String scriptGroupName, boolean isStage) {
        Preconditions.checkNotNull(variableNode);
        List<DeclarationStmt> parsedVariables = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> iter = variableNode.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> currentEntry = iter.next();
            Preconditions.checkArgument(currentEntry.getValue().isArray());
            ArrayNode arrNode = (ArrayNode) currentEntry.getValue();
            if (arrNode.get(1).isNumber()) {
                ProgramParser.symbolTable.addVariable(arrNode.get(0).textValue(), new NumberType(), isStage,
                        scriptGroupName);
                parsedVariables.add(new DeclarationStmt(new Identifier(arrNode.get(0).textValue()), new NumberType()));
            } else if (arrNode.get(1).isBoolean()) {
                ProgramParser.symbolTable.addVariable(arrNode.get(0).textValue(), new BooleanType(), isStage,
                        scriptGroupName);
                parsedVariables.add(new DeclarationStmt(new Identifier(arrNode.get(0).textValue()), new BooleanType()));
            } else {
                ProgramParser.symbolTable.addVariable(arrNode.get(0).textValue(), new StringType(), isStage,
                        scriptGroupName);
                parsedVariables.add(new DeclarationStmt(new Identifier(arrNode.get(0).textValue()), new StringType()));
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
            String listName = arrNode.get(0).textValue();
            JsonNode listValues = arrNode.get(1);
            Preconditions.checkArgument(listValues.isArray());
            ArrayNode valuesArray = (ArrayNode) listValues;
            List<Expression> expressions = new ArrayList<>();
            for (int i = 0; i < valuesArray.size(); i++) {
                //TODO  check if expressionParser should be used
                expressions.add(new Str(valuesArray.get(i).textValue()));
            }
            ExpressionListPlain expressionListPlain = new ExpressionListPlain(expressions);
            ExpressionList expressionList = new ExpressionList(expressionListPlain);
            ProgramParser.symbolTable.addExpressionListInfo(listName, expressionList, isStage, scriptGroupName);
            parsedLists.add(new DeclarationStmt(new Identifier(listName), new ListType()));
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
