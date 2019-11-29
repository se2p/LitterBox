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
package scratch.ast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.ast.ParsingException;
import scratch.ast.model.*;
import scratch.ast.model.procedure.ProcedureDefinitionList;
import scratch.ast.model.resource.Resource;
import scratch.ast.model.resource.ResourceList;
import scratch.ast.model.statement.common.SetStmt;
import scratch.ast.model.statement.declaration.DeclarationStmt;
import scratch.ast.model.statement.declaration.DeclarationStmtList;
import scratch.ast.model.variable.Identifier;
import scratch.ast.model.variable.StrId;
import scratch.ast.opcodes.DependentBlockOpcodes;
import utils.Preconditions;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static scratch.ast.Constants.*;

public class ActorDefinitionParser {

    private static Identifier currentActor;

    public static ActorDefinition parse(JsonNode actorDefinitionNode) throws ParsingException {
        Preconditions.checkNotNull(actorDefinitionNode);
        Preconditions.checkArgument(actorDefinitionNode.has(IS_STAGE_KEY), "Missing field isStage in ScriptGroup");
        Preconditions.checkArgument(actorDefinitionNode.has(NAME_KEY), "Missing field name in ScriptGroup");

        ActorType actorType;
        if (actorDefinitionNode.get(IS_STAGE_KEY).asBoolean()) {
            actorType = ActorType.STAGE;
        } else {
            actorType = ActorType.SPRITE;
        }

        Identifier identifier = new StrId(actorDefinitionNode.get(NAME_KEY).asText());
        currentActor = identifier;

        List<Resource> res = ResourceParser.parseSound(actorDefinitionNode.get("sounds"));
        res.addAll(ResourceParser.parseCostume(actorDefinitionNode.get("costumes")));
        ResourceList resources = new ResourceList(res);

        List<DeclarationStmt> decls = DeclarationStmtParser
            .parseLists(actorDefinitionNode.get("lists"), identifier.getName(),
                actorDefinitionNode.get(IS_STAGE_KEY).asBoolean());
        decls.addAll(DeclarationStmtParser.parseBroadcasts(actorDefinitionNode.get("broadcasts"), identifier.getName(),
            actorDefinitionNode.get(IS_STAGE_KEY).asBoolean()));
        decls.addAll(DeclarationStmtParser.parseVariables(actorDefinitionNode.get("variables"), identifier.getName(),
            actorDefinitionNode.get(IS_STAGE_KEY).asBoolean()));
        decls.addAll(DeclarationStmtParser.parseAttributeDeclarations(actorDefinitionNode));
        DeclarationStmtList declarations = new DeclarationStmtList(decls);

        JsonNode allBlocks = actorDefinitionNode.get("blocks");
        Iterator<String> fieldIterator = allBlocks.fieldNames();
        Iterable<String> iterable = () -> fieldIterator;
        Stream<String> stream = StreamSupport.stream(iterable.spliterator(), false);

        // Get all topLevel Blocks that are not menues
        // the reason for this is, that menues count as topLevel in the Json File
        // if the menu is replaced by another expression
        List<String> topLevelNodes = stream.filter(fieldName ->
                (allBlocks.get(fieldName).get("topLevel").asBoolean()) &&
                        !DependentBlockOpcodes.contains(allBlocks.get(fieldName).get(OPCODE_KEY).asText()))
            .collect(Collectors.toList());

        List<Script> scripts = new LinkedList<>();
        for (String topLevelID : topLevelNodes) {
            Script script = ScriptParser.parse(topLevelID, allBlocks);
            scripts.add(script);
        }
        ScriptList scriptList = new ScriptList(scripts);

        ProcedureDefinitionList procDeclList = ProcDefinitionParser.parse(allBlocks);

        List<SetStmt> setStmtList = DeclarationStmtParser.parseAttributeDeclarationSetStmts(actorDefinitionNode);
        setStmtList.addAll(DeclarationStmtParser.parseListDeclarationSetStmts(actorDefinitionNode.get("lists"),
            identifier.getName()));
        setStmtList.addAll(DeclarationStmtParser.parseVariableDeclarationSetStmts(actorDefinitionNode.get("variables"),
            identifier.getName()));
        return new ActorDefinition(actorType, identifier, resources, declarations, new SetStmtList(setStmtList),
            procDeclList,
            scriptList);
    }

    /**
     * This may be a temporary solution
     *
     * @return
     */
    public static Identifier getCurrentActor() {
        return new StrId(currentActor.getName());
    }
}
