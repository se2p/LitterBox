/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.DependentBlockOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.ActorMetadataParser;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class ActorDefinitionParser {

    public static ActorDefinition parse(final ProgramParserState state, JsonNode actorDefinitionNode)
            throws ParsingException {
        Preconditions.checkNotNull(actorDefinitionNode);
        Preconditions.checkArgument(actorDefinitionNode.has(IS_STAGE_KEY), "Missing field isStage in ScriptGroup");
        Preconditions.checkArgument(actorDefinitionNode.has(NAME_KEY), "Missing field name in ScriptGroup");

        ActorType actorType;
        if (actorDefinitionNode.get(IS_STAGE_KEY).asBoolean()) {
            actorType = ActorType.getStage();
        } else {
            actorType = ActorType.getSprite();
        }

        LocalIdentifier currentActor = new StrId(actorDefinitionNode.get(NAME_KEY).asText());
        state.setCurrentActor(currentActor);

        List<DeclarationStmt> decls = DeclarationStmtParser
                .parseLists(state, actorDefinitionNode.get(LISTS_KEY), currentActor.getName(),
                        actorDefinitionNode.get(IS_STAGE_KEY).asBoolean());
        decls.addAll(DeclarationStmtParser.parseBroadcasts(state, actorDefinitionNode.get(BROADCASTS_KEY),
                currentActor.getName(),
                actorDefinitionNode.get(IS_STAGE_KEY).asBoolean()));
        decls.addAll(DeclarationStmtParser.parseVariables(state, actorDefinitionNode.get(VARIABLES_KEY),
                currentActor.getName(),
                actorDefinitionNode.get(IS_STAGE_KEY).asBoolean()));
        decls.addAll(DeclarationStmtParser.parseAttributeDeclarations(actorDefinitionNode));

        JsonNode allBlocks = actorDefinitionNode.get(BLOCKS_KEY);
        Iterator<String> fieldIterator = allBlocks.fieldNames();
        Iterable<String> iterable = () -> fieldIterator;
        Stream<String> stream = StreamSupport.stream(iterable.spliterator(), false);

        // Get all topLevel Blocks that are not menues
        // the reason for this is, that menues count as topLevel in the Json File
        // if the menu is replaced by another expression
        List<String> topLevelNodes = stream.filter(fieldName ->
                (allBlocks.get(fieldName).has(TOPLEVEL_KEY)
                        && allBlocks.get(fieldName).get(TOPLEVEL_KEY).asBoolean())
                        && !DependentBlockOpcode.contains(allBlocks.get(fieldName).get(OPCODE_KEY).asText())
                        || allBlocks.get(fieldName) instanceof ArrayNode)
                .toList();

        List<Script> scripts = new LinkedList<>();
        for (String topLevelid : topLevelNodes) {
            Script script = ScriptParser.parse(state, topLevelid, allBlocks);
            if (script != null) {
                scripts.add(script);
            }
        }
        ScriptList scriptList = new ScriptList(scripts);

        ProcedureDefinitionList procDeclList = ProcDefinitionParser.parse(state, allBlocks);

        List<SetStmt> setStmtList = DeclarationStmtParser.parseAttributeDeclarationSetStmts(actorDefinitionNode);
        setStmtList.addAll(DeclarationStmtParser.parseListDeclarationSetStmts(actorDefinitionNode.get(LISTS_KEY),
                currentActor.getName()));
        setStmtList.addAll(DeclarationStmtParser.parseVariableDeclarationSetStmts(
                actorDefinitionNode.get(VARIABLES_KEY), currentActor.getName()));
        ActorMetadata metadata = ActorMetadataParser.parse(actorDefinitionNode);

        DeclarationStmtList declarations = new DeclarationStmtList(decls);
        return new ActorDefinition(actorType, currentActor, declarations, new SetStmtList(setStmtList),
                procDeclList, scriptList, metadata);
    }
}
