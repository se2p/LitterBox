package scratch.newast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.newast.model.Program;
import scratch.newast.model.ScriptGroup;
import scratch.newast.model.ScriptGroupList;
import scratch.newast.model.variable.Identifier;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ProgramParser {

    public static Program parseProgram(String zipFileName, JsonNode programNode) {
        Identifier ident = new Identifier(zipFileName);

        if (!programNode.has("targets")) {
            throw new IllegalArgumentException("Program node has no field targets");
        }

        Iterable<JsonNode> iterable = () -> programNode.get("targets").iterator();
        Stream<JsonNode> stream = StreamSupport.stream(iterable.spliterator(), false);
        Optional<JsonNode> stageNode = stream.filter(node -> node.get("isStage").asBoolean()).findFirst(); //TODO: Check that only one stage exists
        if (!stageNode.isPresent()) {
            throw new IllegalArgumentException("Program has no Stage");
        }

        ScriptGroup stage = ScriptGroupParser.parse(stageNode.get());

        iterable = () -> programNode.get("targets").iterator();
        stream = StreamSupport.stream(iterable.spliterator(), false);
        List<JsonNode> nonStageNodes = stream.filter(node -> !(node.get("isStage").asBoolean())).collect(Collectors.toList());

        List<ScriptGroup> scriptGroups = new LinkedList<>();
        scriptGroups.add(stage);
        for (JsonNode nonStageNode : nonStageNodes) {
            ScriptGroup group = ScriptGroupParser.parse(nonStageNode);
            scriptGroups.add(group);
        }

        ScriptGroupList scriptGroupList = new ScriptGroupList(scriptGroups);

        return new Program(ident, scriptGroupList);
    }
}
