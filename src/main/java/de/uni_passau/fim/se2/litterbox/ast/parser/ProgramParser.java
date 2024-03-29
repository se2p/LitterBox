/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProgramMetadata;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.ProgramMetadataParser;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.IS_STAGE_KEY;
import static de.uni_passau.fim.se2.litterbox.ast.Constants.TARGETS_KEY;

public class ProgramParser {

    public static Program parseProgram(String programName, JsonNode programNode) throws ParsingException {
        final ProgramParserState state = new ProgramParserState();

        Preconditions.checkNotNull(programName);
        Preconditions.checkNotNull(programNode);

        LocalIdentifier ident = new StrId(programName);

        if (!programNode.has(TARGETS_KEY) && programNode.has("objName")) {
            throw new ParsingException("LitterBox only supports Scratch 3 projects.");
        }

        Iterable<JsonNode> iterable = () -> programNode.get(TARGETS_KEY).iterator();
        Stream<JsonNode> stream = StreamSupport.stream(iterable.spliterator(), false);
        Optional<JsonNode> stageNode = stream.filter(node -> node.get(IS_STAGE_KEY).asBoolean())
                .findFirst(); //Is it necessary to check that only one stage exists?

        if (stageNode.isEmpty()) {
            throw new ParsingException("Program has no Stage");
        }

        List<ActorDefinition> actorDefinitions = getActorDefinitions(state, programNode, stageNode.get());

        ActorDefinitionList actorDefinitionList = new ActorDefinitionList(actorDefinitions);
        ProgramMetadata metadata = ProgramMetadataParser.parse(programNode);
        return new Program(ident, actorDefinitionList, state.getSymbolTable(), state.getProcDefMap(), metadata);
    }

    private static List<ActorDefinition> getActorDefinitions(final ProgramParserState state, JsonNode programNode,
                                                             JsonNode stageNode) throws ParsingException {
        ActorDefinition stage = ActorDefinitionParser.parse(state, stageNode);

        Iterable<JsonNode> iterable = () -> programNode.get(TARGETS_KEY).iterator();
        Stream<JsonNode> stream = StreamSupport.stream(iterable.spliterator(), false);
        List<JsonNode> nonStageNodes = stream.filter(node -> !(node.get(IS_STAGE_KEY).asBoolean()))
                .collect(Collectors.toList());

        List<ActorDefinition> actorDefinitions = new LinkedList<>();
        actorDefinitions.add(stage);
        for (JsonNode nonStageNode : nonStageNodes) {
            ActorDefinition group = ActorDefinitionParser.parse(state, nonStageNode);
            actorDefinitions.add(group);
        }
        return actorDefinitions;
    }
}
