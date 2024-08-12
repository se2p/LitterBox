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
package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

/**
 * Calculate the length of the longest script or procedure of the project.
 */
public class LengthLongestScript<T extends ASTNode> implements MetricExtractor<T>, ScratchVisitor {
    public static final String NAME = "length_longest_script";
    int count = 0;
    private int localCount = 0;

    @Override
    public double calculateMetric(T node) {
        Preconditions.checkNotNull(node);
        this.count = 0;
        node.accept(this);
        return count;
    }

    @Override
    public void visit(Script node) {
        localCount = 0;
        if (!(node.getEvent() instanceof Never)) {
            localCount++;
        }
        visitChildren(node);
        if (localCount > count) {
            count = localCount;
        }
    }

    @Override
    public void visit(ProcedureDefinition node) {
        localCount = 0;

        //this is for counting the definition block itself
        localCount++;
        visitChildren(node);
        if (localCount > count) {
            count = localCount;
        }
    }

    @Override
    public void visit(StmtList node) {
        localCount = localCount + node.getStmts().size();
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
