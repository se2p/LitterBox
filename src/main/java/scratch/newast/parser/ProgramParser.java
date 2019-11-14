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
import scratch.newast.model.ActorDefinition;
import scratch.newast.model.ActorDefinitionList;
import scratch.newast.model.Program;
import scratch.newast.model.variable.Identifier;
import scratch.newast.parser.symboltable.ProcedureDefinitionNameMapping;
import scratch.newast.parser.symboltable.SymbolTable;

public class ProgramParser {

    public static SymbolTable symbolTable;
    public static ProcedureDefinitionNameMapping procDefMap;

    public static Program parseProgram(String programName, JsonNode programNode) throws ParsingException {
        Preconditions.checkNotNull(programName);
        Preconditions.checkNotNull(programNode);

        symbolTable = new SymbolTable();
        procDefMap = new ProcedureDefinitionNameMapping();

        Identifier ident = new Identifier(programName);

        Preconditions.checkArgument(programNode.has("targets"),
            "Program node has no field targets");

        Iterable<JsonNode> iterable = () -> programNode.get("targets").iterator();
        Stream<JsonNode> stream = StreamSupport.stream(iterable.spliterator(), false);
        Optional<JsonNode> stageNode = stream.filter(node -> node.get("isStage").asBoolean())
            .findFirst(); //TODO: Check that only one stage exists

        if (!stageNode.isPresent()) {
            throw new ParsingException("Program has no Stage");
        }

        ActorDefinition stage = ActorDefinitionParser.parse(stageNode.get());

        iterable = () -> programNode.get("targets").iterator();
        stream = StreamSupport.stream(iterable.spliterator(), false);
        List<JsonNode> nonStageNodes = stream.filter(node -> !(node.get("isStage").asBoolean()))
            .collect(Collectors.toList());

        List<ActorDefinition> actorDefinitions = new LinkedList<>();
        actorDefinitions.add(stage);
        for (JsonNode nonStageNode : nonStageNodes) {
            ActorDefinition group = ActorDefinitionParser.parse(nonStageNode);
            actorDefinitions.add(group);
        }

        ActorDefinitionList actorDefinitionList = new ActorDefinitionList(actorDefinitions);

        return new Program(ident, actorDefinitionList);
    }
}
