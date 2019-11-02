package scratch.newast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.newast.model.Declaration;
import scratch.newast.model.Script;
import scratch.newast.model.ScriptGroup;
import scratch.newast.model.resource.Resource;
import scratch.newast.model.variable.Identifier;

import java.util.List;

public class ScriptGroupParser {

    public static ScriptGroup parse(JsonNode jsonNode) {

        if (!jsonNode.has("isStage") || !jsonNode.has("name")) {
            throw new IllegalArgumentException("Expected fields 'isStage' and 'name' in ScriptGroup.");
        }

        Identifier identifier = new Identifier(jsonNode.get("name").asText());

        List<Resource> resources = ResourceParser.parse(jsonNode.get("sounds"));
        resources.addAll(ResourceParser.parse(jsonNode.get("costumes")));

        List<Declaration> declarations = DeclarationParser.parse(jsonNode.get("lists"));
        declarations.addAll(DeclarationParser.parse(jsonNode.get("broadcasts")));
        declarations.addAll(DeclarationParser.parse(jsonNode.get("variables")));

        List<Script> scripts = ScriptParser.parse(jsonNode.get("blocks"));

        return new ScriptGroup(identifier, resources, declarations, scripts);
    }

}
