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

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.SpecialNode;

import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;

import java.util.function.Function;

public class ScratchBlocksLabeler implements Function<CFGNode, String> {

    @Override
    public String apply(CFGNode node) {
        if (node instanceof SpecialNode) {
            return node.toString();
        }
        ASTNode astNode = node.getASTNode();
        if (astNode == null) {
            return node.toString();
        }

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(false, false) {
            @Override
            public void visit(IfThenStmt ifThenStmt) {
                emitNoSpace("if ");
                ifThenStmt.getBoolExpr().accept(this);
                emitNoSpace(" then");
            }

            @Override
            public void visit(IfElseStmt ifElseStmt) {
                emitNoSpace("if ");
                ifElseStmt.getBoolExpr().accept(this);
                emitNoSpace(" then");
                newLine();
                emitNoSpace("else");
                newLine();
                emitNoSpace("end");
            }

            @Override
            public void visit(RepeatForeverStmt repeatForeverStmt) {
                emitNoSpace("forever");
            }

            @Override
            public void visit(UntilStmt untilStmt) {
                emitNoSpace("repeat until ");
                untilStmt.getBoolExpr().accept(this);
            }

            @Override
            public void visit(RepeatTimesStmt repeatTimesStmt) {
                emitNoSpace("repeat ");
                repeatTimesStmt.getTimes().accept(this);
            }
        };

        astNode.accept(visitor);
        String code = visitor.getScratchBlocks();

        // The scratchblocks visitor might return empty string for some nodes.
        if (code == null || code.trim().isEmpty()) {
            return node.toString();
        }

        return code.trim();
    }
}
