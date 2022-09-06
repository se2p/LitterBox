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

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import static de.uni_passau.fim.se2.litterbox.analytics.mblock.RobotCode.MCORE;
import static de.uni_passau.fim.se2.litterbox.analytics.mblock.RobotCode.getRobot;

public class MCoreCounterMetric extends AbstractRobotMetric<Program> {

    public static final String NAME = "robot_mcore_counter";
    private int mcoreCounter = 0;

    @Override
    public double calculateMetric(Program node) {
        node.accept(this);
        return mcoreCounter;
    }

    @Override
    public void visit(ActorDefinition actor) {
        if (getRobot(actor.getIdent().getName(), actor.getSetStmtList()) == MCORE) {
            mcoreCounter++;
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
