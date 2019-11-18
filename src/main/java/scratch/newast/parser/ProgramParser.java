/*
 * Copyright (C) 2019 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
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
