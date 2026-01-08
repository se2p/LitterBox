/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.utils;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProcedureMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.type.StringType;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureDefinitionNameMapping;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScratchBlocksLabelerTest {

    @Test
    void testApplyAddsNoRedundantTags() {
        BlockMetadata metadata = mock(BlockMetadata.class);
        MoveSteps moveSteps = new MoveSteps(new NumberLiteral(10), metadata);
        CFGNode node = mock(CFGNode.class);
        when(node.getASTNode()).thenReturn(moveSteps);

        ScratchBlocksLabeler labeler = new ScratchBlocksLabeler(null);
        String result = labeler.apply(node);

        // ScratchBlocksVisitor usually adds formatting.
        // We expect "move (10) steps"
        // And we assert that it DOES NOT contain [scratchblocks] tags.
        assertTrue(result.contains("move (10) steps"), "Should contain block text. Got: " + result);
        assertFalse(result.contains("[scratchblocks]"), "Should not contain [scratchblocks] tag, but got: " + result);
        assertFalse(result.contains("[/scratchblocks]"), "Should not contain [/scratchblocks] tag");
    }

    @Test
    void testTrimControlStructures() {
        // Create an IfThenStmt with a body
        BlockMetadata metadata = mock(BlockMetadata.class);

        MoveSteps moveSteps = new MoveSteps(new NumberLiteral(10), metadata);
        StmtList stmtList = new StmtList(moveSteps);

        // We need a proper BoolExpr that visits successfully.
        UnspecifiedBoolExpr unspec = new UnspecifiedBoolExpr();
        IfThenStmt ifThen = new IfThenStmt(unspec, stmtList, metadata);

        CFGNode node = mock(CFGNode.class);
        when(node.getASTNode()).thenReturn(ifThen);

        ScratchBlocksLabeler labeler = new ScratchBlocksLabeler(null);
        String result = labeler.apply(node);

        // Expect: "if <> then\nend"
        // Should NOT contain: "move (10) steps"
        assertTrue(result.contains("if <> then"), "Should contain if header. Got: " + result);
        assertFalse(result.contains("end"), "Should NOT contain end. Got: " + result);
        assertFalse(result.contains("move"), "Should NOT contain body statements. Got: " + result);
    }

    @Test
    void testTrimProcedureDefinition() {
        // Create a ProcedureDefinition setup
        BlockMetadata metadata = mock(BlockMetadata.class);
        ProcedureMetadata procMetadata = mock(ProcedureMetadata.class);
        LocalIdentifier ident = new StrId("my block %s");

        // Mock ParameterDefinitionList
        ParameterDefinitionList paramList = mock(ParameterDefinitionList.class);
        ParameterDefinition paramDef = mock(ParameterDefinition.class);
        when(paramDef.getType()).thenReturn(new StringType());
        when(paramDef.getIdent()).thenReturn(new StrId("param1"));

        // Create body
        MoveSteps moveSteps = new MoveSteps(new NumberLiteral(10), metadata);
        StmtList stmtList = new StmtList(moveSteps);

        ProcedureDefinition procDef = new ProcedureDefinition(ident, paramList, stmtList, procMetadata);

        ActorDefinition actor = mock(ActorDefinition.class);
        when(actor.getIdent()).thenReturn(new StrId("Sprite1"));
        procDef.setParentNode(actor);

        // Mock Program
        Program program = mock(Program.class);
        ProcedureDefinitionNameMapping mapping = mock(ProcedureDefinitionNameMapping.class);
        Map<String, Map<LocalIdentifier, ProcedureInfo>> procMap = new HashMap<>();
        Map<LocalIdentifier, ProcedureInfo> spriteMap = new HashMap<>();
        ProcedureInfo procModel = mock(ProcedureInfo.class);
        when(procModel.getName()).thenReturn("my block %s");
        spriteMap.put(ident, procModel);
        procMap.put("Sprite1", spriteMap);

        when(mapping.getProcedures()).thenReturn(procMap);
        when(program.getProcedureMapping()).thenReturn(mapping);

        when(paramList.getParameterDefinitions()).thenReturn(Collections.emptyList());

        CFGNode node = mock(CFGNode.class);
        when(node.getASTNode()).thenReturn(procDef);

        ScratchBlocksLabeler labeler = new ScratchBlocksLabeler(program);
        String result = labeler.apply(node);

        // Expect "define my block %s" (or substituted if we had params)
        assertTrue(result.contains("define"), "Should contain define. Got: " + result);
        assertFalse(result.contains("move"), "Should NOT contain body statements. Got: " + result);
    }
}
