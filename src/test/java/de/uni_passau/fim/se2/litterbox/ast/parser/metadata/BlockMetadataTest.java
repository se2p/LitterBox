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
package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProcedureMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.*;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.BlockByIdFinder;
import de.uni_passau.fim.se2.litterbox.ast.visitor.NodeFilteringVisitor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockMetadataTest {
    private static Program prog;
    private static ActorDefinition sprite;

    @BeforeAll
    static void setUp() throws IOException, ParsingException {
        prog = JsonTest.parseProgram("src/test/fixtures/metadata/blockMeta.json");
        sprite = prog.getActorDefinitionList().getDefinitions().stream()
                .filter(actor -> "Sprite1".equals(actor.getIdent().getName()))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void testDataBlock() {
        final Variable myVariable = NodeFilteringVisitor.getBlock(sprite.getScripts(), Variable.class).orElseThrow();
        assertInstanceOf(DataBlockMetadata.class, myVariable.getMetadata());

        final DataBlockMetadata dataBlock = (DataBlockMetadata) myVariable.getMetadata();
        assertEquals(471, dataBlock.getX());
        assertEquals(383, dataBlock.getY());
    }

    @Test
    void testNoMetadataTopBlock() {
        final ASTNode block = BlockByIdFinder.findBlock(sprite, "X)N~xB@[E,i0S}Vwwtjm").orElseThrow();
        assertInstanceOf(ProcedureDefinition.class, block);

        final ProcedureDefinition procDef = (ProcedureDefinition) block;
        assertInstanceOf(TopNonDataBlockMetadata.class, procDef.getMetadata().getDefinition());

        final TopNonDataBlockMetadata metadata = (TopNonDataBlockMetadata) procDef.getMetadata().getDefinition();
        assertEquals("X)N~xB@[E,i0S}Vwwtjm", metadata.getBlockId());
        assertNull(metadata.getCommentId());
        assertEquals(56, metadata.getXPos());
        assertEquals(184, metadata.getYPos());
        assertInstanceOf(NoMutationMetadata.class, metadata.getMutation());
        assertFalse(metadata.isShadow());
        assertInstanceOf(ProcedureMetadata.class, metadata.getParentNode());
    }

    @Test
    void testMetadataBlock() {
        final ASTNode block = BlockByIdFinder.findBlock(sprite, "X)N~xB@[E,i0S}Vwwtjm").orElseThrow();
        assertInstanceOf(ProcedureDefinition.class, block);

        final ProcedureDefinition procDef = (ProcedureDefinition) block;
        final BlockMetadata prototypeMetadata = procDef.getMetadata().getPrototype();
        assertInstanceOf(NonDataBlockMetadata.class, prototypeMetadata);

        final NonDataBlockMetadata nonDataBlockMetadata = (NonDataBlockMetadata) prototypeMetadata;
        assertEquals("Vr$zTl8mo1W,U?+q6,T{", nonDataBlockMetadata.getBlockId());
        assertNull(nonDataBlockMetadata.getCommentId());
        assertInstanceOf(ProcedureMutationMetadata.class, nonDataBlockMetadata.getMutation());
        assertTrue(nonDataBlockMetadata.isShadow());
    }

    @Test
    void testMetadataVariableInsideAndOutsideBlock() throws ParsingException, IOException {
        Program prog = JsonTest.parseProgram("src/test/fixtures/metadata/variableMetadata.json");
        List<Script> scripts = prog.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList();
        Assertions.assertEquals(2, scripts.size());
        Assertions.assertInstanceOf(ExpressionStmt.class, scripts.get(0).getStmtList().getStmts().get(0));
        ExpressionStmt expressionStmt = (ExpressionStmt) scripts.get(0).getStmtList().getStmts().get(0);
        Qualified qualified = (Qualified) expressionStmt.getExpression();
        Assertions.assertInstanceOf(Variable.class, qualified.getSecond());
        Variable var = (Variable) qualified.getSecond();
        Assertions.assertInstanceOf(DataBlockMetadata.class, var.getMetadata());

        Assertions.assertInstanceOf(MoveSteps.class, scripts.get(1).getStmtList().getStmts().get(0));
        MoveSteps moveSteps = (MoveSteps) scripts.get(1).getStmtList().getStmts().get(0);
        Assertions.assertInstanceOf(AsNumber.class, moveSteps.getSteps());
        AsNumber asNumber = (AsNumber) moveSteps.getSteps();
        Assertions.assertInstanceOf(Qualified.class, asNumber.getOperand1());
        qualified = (Qualified) asNumber.getOperand1();
        Assertions.assertInstanceOf(Variable.class, qualified.getSecond());
        var = (Variable) qualified.getSecond();
        Assertions.assertInstanceOf(NoBlockMetadata.class, var.getMetadata());
    }
}
