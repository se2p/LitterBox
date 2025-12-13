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
package de.uni_passau.fim.se2.litterbox.utils;

import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import org.junit.jupiter.api.Test;

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

        ScratchBlocksLabeler labeler = new ScratchBlocksLabeler();
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
        StmtList stmtList = new de.uni_passau.fim.se2.litterbox.ast.model.StmtList(moveSteps);

        // We need a proper BoolExpr that visits successfully.
        UnspecifiedBoolExpr unspec = new UnspecifiedBoolExpr();
        IfThenStmt ifThen = new IfThenStmt(unspec, stmtList, metadata);

        CFGNode node = mock(CFGNode.class);
        when(node.getASTNode()).thenReturn(ifThen);

        ScratchBlocksLabeler labeler = new ScratchBlocksLabeler();
        String result = labeler.apply(node);

        // Expect: "if <> then\nend"
        // Should NOT contain: "move (10) steps"
        assertTrue(result.contains("if <> then"), "Should contain if header. Got: " + result);
        assertFalse(result.contains("end"), "Should NOT contain end. Got: " + result);
        assertFalse(result.contains("move"), "Should NOT contain body statements. Got: " + result);
    }
}
