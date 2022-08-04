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
package de.uni_passau.fim.se2.litterbox.analytics.mblock.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.MBlockNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.MBlockVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import static de.uni_passau.fim.se2.litterbox.analytics.mblock.RobotCode.AURIGA;
import static de.uni_passau.fim.se2.litterbox.analytics.mblock.RobotCode.getRobot;

public class AurigaCounterMetric implements MetricExtractor<Program>, ScratchVisitor, MBlockVisitor {

    public static final String NAME = "robot_auriga_counter";
    private int aurigaCounter = 0;

    @Override
    public double calculateMetric(Program node) {
        node.accept(this);
        return aurigaCounter;
    }

    @Override
    public void visit(ActorDefinition actor) {
        if (getRobot(actor.getIdent().getName(), actor.getSetStmtList()) == AURIGA) {
            aurigaCounter++;
        }
    }

    @Override
    public void visit(MBlockNode node) {
        node.accept((MBlockVisitor) this);
    }

    @Override
    public void visitParentVisitor(MBlockNode node) {
        visitDefaultVisitor(node);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
