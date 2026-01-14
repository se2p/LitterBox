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
package de.uni_passau.fim.se2.litterbox.analytics.metrics;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Volume;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.ActorSoundStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class SoundBlockCount<T extends ASTNode> implements MetricExtractor<T>, ScratchVisitor {
    public static final String NAME = "sound_block_count";

    private final boolean average;
    private int count = 0;

    public SoundBlockCount() {
        this.average = false;
    }

    public SoundBlockCount(boolean average) {
        this.average = average;
    }

    @Override
    public double calculateMetric(T node) {
        Preconditions.checkNotNull(node);
        double totalCount = new BlockCount<T>().calculateMetric(node);
        if (totalCount == 0) {
            return 0.0;
        }
        count = 0;
        node.accept(this);
        return average ? count / totalCount : count;
    }

    @Override
    public void visit(ActorSoundStmt node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(Volume node) {
        count++;
    }

    @Override
    public String getName() {
        if (average) {
            return "average_" + NAME;
        }
        return NAME;
    }
}
