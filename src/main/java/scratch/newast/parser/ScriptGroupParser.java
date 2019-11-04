package scratch.newast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.newast.model.*;
import scratch.newast.model.procedure.ProcedureDeclarationList;
import scratch.newast.model.resource.Resource;
import scratch.newast.model.resource.ResourceList;
import scratch.newast.model.variable.Identifier;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ScriptGroupParser {

    public static ScriptGroup parse(JsonNode jsonNode) {

        if (!jsonNode.has("isStage") || !jsonNode.has("name")) {
            throw new IllegalArgumentException("Expected fields 'isStage' and 'name' in ScriptGroup.");
        }

        Entity entity;
        if (jsonNode.get("isStage").asBoolean()) {
            entity = Entity.stage;
        } else {
            entity = Entity.sprite;
        }

        Identifier identifier = new Identifier(jsonNode.get("name").asText());

        List<Resource> res = ResourceParser.parse(jsonNode.get("sounds"));
        res.addAll(ResourceParser.parse(jsonNode.get("costumes")));
        ResourceList resources = new ResourceList(res);

        List<Declaration> decls = DeclarationParser.parse(jsonNode.get("lists"));
        decls.addAll(DeclarationParser.parse(jsonNode.get("broadcasts")));
        decls.addAll(DeclarationParser.parse(jsonNode.get("variables")));
        DeclarationList declarations = new DeclarationList(decls);

        JsonNode allBlocks = jsonNode.get("blocks");
        Iterator<String> fieldIterator = allBlocks.fieldNames();
        Iterable<String> iterable = () -> fieldIterator;
        Stream<String> stream = StreamSupport.stream(iterable.spliterator(), false);
        List<String> topLevelNodes = stream.filter(fieldName -> allBlocks.get(fieldName).get("topLevel").asBoolean()).collect(Collectors.toList());
        List<Script> scripts = new LinkedList<>();
        for (String topLevelID : topLevelNodes) {
            Script script = ScriptParser.parse(topLevelID, allBlocks);
            scripts.add(script);
        }
        ScriptList scriptList = new ScriptList(scripts);

        ProcedureDeclarationList procDeclList = ProcDeclParser.parse(allBlocks);

        return new ScriptGroup(entity, identifier, resources, declarations, procDeclList, scriptList);
    }

}
