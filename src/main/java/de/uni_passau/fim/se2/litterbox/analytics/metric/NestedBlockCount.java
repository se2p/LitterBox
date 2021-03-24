/*
 * Copyright (C) 2020 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

public class NestedBlockCount<T extends ASTNode> implements MetricExtractor<T>, ScratchVisitor {

    public static final String NAME = "nested_block_count";

    private int count = 0;
    private int nestedLoopCount = 0;
    private List<Integer> nestedLoopCountList = new ArrayList<>();
    private int maxNestedDepth = 0;
    private int currentNestedDepth = 0;

//    private String callingMethod = "";

    @Override
    public void visit(IfThenStmt node) {
//        String currentMethod = "ifThen";
        currentNestedDepth++;
        if (currentNestedDepth > maxNestedDepth) {
            maxNestedDepth = currentNestedDepth;
        }
        visitChildren(node);
        currentNestedDepth--;
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        String currentMethod = "repeatTimes";
        currentNestedDepth++;
        if (currentNestedDepth > maxNestedDepth) {
            maxNestedDepth = currentNestedDepth;
        }
        visitChildren(node);
        currentNestedDepth--;
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        String currentMethod = "repeatForever";
        currentNestedDepth++;
        if (currentNestedDepth > maxNestedDepth) {
            maxNestedDepth = currentNestedDepth;
        }
        visitChildren(node);
        currentNestedDepth--;
    }

    @Override
    public void visit(IfElseStmt node) {
        currentNestedDepth++;
        if (currentNestedDepth > maxNestedDepth) {
            maxNestedDepth = currentNestedDepth;
        }
        visitChildren(node);
        currentNestedDepth--;
    }

    @Override
    public void visit(UntilStmt node) {
        String currentMethod = "until";
        currentNestedDepth++;
        if (currentNestedDepth > maxNestedDepth) {
            maxNestedDepth = currentNestedDepth;
        }
        visitChildren(node);
        currentNestedDepth--;
    }

    @Override
    public double calculateMetric(T node) {
        Preconditions.checkNotNull(node);
        count = 0;
        node.accept(this);
        return count;
    }

    @Override
    public void visit(Script node) {
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
