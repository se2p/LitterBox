package scratch.newast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import scratch.newast.ParsingException;
import scratch.newast.model.Program;
import scratch.newast.model.ScriptGroup;
import scratch.newast.model.ScriptGroupList;
import scratch.newast.model.variable.Identifier;
import scratch.newast.parser.symboltable.SymbolTable;

public class ProgramParser {

    public static SymbolTable symbolTable;

    public static Program parseProgram(String zipFileName, JsonNode programNode) throws ParsingException {
        Preconditions.checkNotNull(zipFileName);
        Preconditions.checkNotNull(programNode);

        symbolTable = new SymbolTable();

        Identifier ident = new Identifier(zipFileName);

        Preconditions.checkArgument(programNode.has("targets"),
            "Program node has no field targets");

        Iterable<JsonNode> iterable = () -> programNode.get("targets").iterator();
        Stream<JsonNode> stream = StreamSupport.stream(iterable.spliterator(), false);
        Optional<JsonNode> stageNode = stream.filter(node -> node.get("isStage").asBoolean())
            .findFirst(); //TODO: Check that only one stage exists

        if (!stageNode.isPresent()) {
            throw new ParsingException("Program has no Stage");
        }

        ScriptGroup stage = ScriptGroupParser.parse(stageNode.get());

        iterable = () -> programNode.get("targets").iterator();
        stream = StreamSupport.stream(iterable.spliterator(), false);
        List<JsonNode> nonStageNodes = stream.filter(node -> !(node.get("isStage").asBoolean()))
            .collect(Collectors.toList());

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
