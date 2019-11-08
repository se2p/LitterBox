package scratch.newast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import scratch.newast.ParsingException;
import scratch.newast.model.Declaration;
import scratch.newast.model.DeclarationList;
import scratch.newast.model.EntityType;
import scratch.newast.model.Script;
import scratch.newast.model.ScriptGroup;
import scratch.newast.model.ScriptList;
import scratch.newast.model.SetStmtList;
import scratch.newast.model.procedure.ProcedureDeclarationList;
import scratch.newast.model.resource.Resource;
import scratch.newast.model.resource.ResourceList;
import scratch.newast.model.variable.Identifier;

public class ScriptGroupParser {

    public static ScriptGroup parse(JsonNode jsonNode) throws ParsingException {
        Preconditions.checkNotNull(jsonNode);
        Preconditions.checkArgument(jsonNode.has("isStage"), "Missing field isStage in ScriptGroup");
        Preconditions.checkArgument(jsonNode.has("name"), "Missing field name in ScriptGroup");

        EntityType entityType;
        if (jsonNode.get("isStage").asBoolean()) {
            entityType = EntityType.stage;
        } else {
            entityType = EntityType.sprite;
        }

        Identifier identifier = new Identifier(jsonNode.get("name").asText());

        List<Resource> res = ResourceParser.parseSound(jsonNode.get("sounds"));
        res.addAll(ResourceParser.parseCostume(jsonNode.get("costumes")));
        ResourceList resources = new ResourceList(res);

        List<Declaration> decls = DeclarationParser.parseLists(jsonNode.get("lists"));
        decls.addAll(DeclarationParser.parseBroadcasts(jsonNode.get("broadcasts")));
        decls.addAll(DeclarationParser.parseVariables(jsonNode.get("variables")));
        DeclarationList declarations = new DeclarationList(decls);

        JsonNode allBlocks = jsonNode.get("blocks");
        Iterator<String> fieldIterator = allBlocks.fieldNames();
        Iterable<String> iterable = () -> fieldIterator;
        Stream<String> stream = StreamSupport.stream(iterable.spliterator(), false);
        List<String> topLevelNodes = stream.filter(fieldName -> allBlocks.get(fieldName).get("topLevel").asBoolean())
            .collect(Collectors.toList());
        List<Script> scripts = new LinkedList<>();
        for (String topLevelID : topLevelNodes) {
            Script script = ScriptParser.parse(topLevelID, allBlocks);
            scripts.add(script);
        }
        ScriptList scriptList = new ScriptList(scripts);

        ProcedureDeclarationList procDeclList = ProcDeclParser.parse(allBlocks);

        SetStmtList setStmtList = parseSetStmts(jsonNode);

        return new ScriptGroup(entityType, identifier, resources, declarations, setStmtList, procDeclList, scriptList);
    }

    private static SetStmtList parseSetStmts(JsonNode jsonNode) {
        throw new RuntimeException("Not implemented yet");
    }

}
