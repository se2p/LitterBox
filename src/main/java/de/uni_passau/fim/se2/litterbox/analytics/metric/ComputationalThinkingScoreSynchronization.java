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
package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.BackdropSwitchTo;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class ComputationalThinkingScoreSynchronization implements MetricExtractor<Program>, ScratchVisitor {
    public final static String NAME = "ct_score_synchronization";
    private int score = 0;

    @Override
    public MetricResult calculateMetric(Program program) {
        score = 0;
        program.accept(this);
        return new MetricResult(NAME, score);
    }

    @Override
    public void visit(WaitUntil node) {
        score = 3;
    }

    @Override
    public void visit(BackdropSwitchTo node) {
        score = 3;
    }

    @Override
    public void visit(BroadcastAndWait node) {
        score = 3;
    }

    @Override
    public void visit(Broadcast node) {
        score = Math.max(2, score);
    }

    @Override
    public void visit(ReceptionOfMessage node) {
        score = Math.max(2, score);
        visitChildren(node);
    }

    @Override
    public void visit(StopThisScript node) {
        score = Math.max(2, score);
    }

    @Override
    public void visit(StopOtherScriptsInSprite node) {
        score = Math.max(2, score);
    }

    @Override
    public void visit(StopAll node) {
        score = Math.max(2, score);
    }

    @Override
    public void visit(WaitSeconds node) {
        score = Math.max(1, score);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
