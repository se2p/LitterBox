package scratch.newast.parser;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Preconditions;
import scratch.newast.model.Declaration;
import scratch.newast.model.Message;
import scratch.newast.model.type.BooleanType;
import scratch.newast.model.type.NumberType;
import scratch.newast.model.type.StringType;
import scratch.newast.model.variable.Identifier;

public class DeclarationParser {

    public static List<Declaration> parseVariables(JsonNode variableNode, String scriptGroupName, boolean isStage) {
        Preconditions.checkNotNull(variableNode);
        List<Declaration> parsedVariables = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> iter = variableNode.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> currentEntry = iter.next();
            Preconditions.checkArgument(currentEntry.getValue().isArray());
            ArrayNode arrNode = (ArrayNode) currentEntry.getValue();
            if (arrNode.get(1).isNumber()) {
                ProgramParser.symbolTable.addVariable(arrNode.get(0).textValue(), new NumberType(), isStage,
                        scriptGroupName);
                parsedVariables.add(new Declaration(new Identifier(arrNode.get(0).textValue()), new NumberType()));
            } else if (arrNode.get(1).isBoolean()) {
                ProgramParser.symbolTable.addVariable(arrNode.get(0).textValue(), new BooleanType(), isStage,
                        scriptGroupName);
                parsedVariables.add(new Declaration(new Identifier(arrNode.get(0).textValue()), new BooleanType()));
            } else {
                ProgramParser.symbolTable.addVariable(arrNode.get(0).textValue(), new StringType(), isStage,
                        scriptGroupName);
                parsedVariables.add(new Declaration(new Identifier(arrNode.get(0).textValue()), new StringType()));
            }
        }
        return parsedVariables;
    }

    public static List<Declaration> parseLists(JsonNode listsNode, String scriptGroupName, boolean isStage) {
        throw new RuntimeException("Not Implemented");
    }

    public static List<Declaration> parseBroadcasts(JsonNode broadcastsNode, String scriptGroupName, boolean isStage) {
        Preconditions.checkNotNull(broadcastsNode);
        List<Declaration> parsedBroadcasts = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> iter = broadcastsNode.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> current = iter.next();
            ProgramParser.symbolTable.addMessage(current.getValue().textValue(),
                    new Message(current.getValue().textValue()), isStage, scriptGroupName);
            parsedBroadcasts.add(new Declaration(new Identifier(current.getValue().textValue()), new StringType()));
        }
        return parsedBroadcasts;
    }
}
