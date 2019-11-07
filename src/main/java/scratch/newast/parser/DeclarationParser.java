package scratch.newast.parser;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import scratch.newast.model.Declaration;
import scratch.newast.model.URI;
import scratch.newast.model.resource.Resource;
import scratch.newast.model.resource.SoundResource;
import scratch.newast.model.type.MessageType;
import scratch.newast.model.type.NumberType;
import scratch.newast.model.type.StringType;
import scratch.newast.model.variable.Identifier;

public class DeclarationParser {

    public static List<Declaration> parseVariables(JsonNode variableNode) {
        Preconditions.checkNotNull(variableNode);
        List<Declaration> parsedVariables = new ArrayList<>();
        Iterator<JsonNode> iter = variableNode.elements();
        while (iter.hasNext()) {
            JsonNode node = iter.next();
            Iterator<JsonNode> iter2 = node.elements();
            String name = iter2.next().textValue();
            JsonNode type = iter2.next();
            Declaration var;
            if (type.isNumber()) {
                var = new Declaration(new Identifier(name),
                        new NumberType());
            } else {
                var = new Declaration(new Identifier(name),
                        new StringType());
            }
            parsedVariables.add(var);
        }
        return parsedVariables;
    }

    public static List<Declaration> parseLists(JsonNode listsNode) {
        throw new RuntimeException("Not Implemented");
    }

    public static List<Declaration> parseBroadcasts(JsonNode broadcastsNode) {
        Preconditions.checkNotNull(broadcastsNode);
        List<Declaration> parsedBroadcasts = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> iter = broadcastsNode.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> current = iter.next();
            parsedBroadcasts.add(new Declaration(new Identifier(current.getValue().textValue()), new StringType()));
        }
        return parsedBroadcasts;
    }
}
