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

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.event.MBlockEvent;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.MBlockExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.MBlockStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;

import static de.uni_passau.fim.se2.litterbox.analytics.mblock.RobotCode.CODEY;
import static de.uni_passau.fim.se2.litterbox.analytics.mblock.RobotCode.getRobot;

public class RealCodeyMetric extends AbstractRobotMetric<Program> {

    public static final String NAME = "real_codey_program";
    private boolean robot = false;
    private boolean realRobot = false;

    @Override
    public double calculateMetric(Program node) {
        node.accept(this);
        return realRobot ? 1 : 0;
    }

    @Override
    public void visit(ActorDefinition actor) {
        robot = robot || getRobot(actor.getIdent().getName(), actor.getSetStmtList()) == CODEY;
        if (robot) {
            visit((ASTNode) actor);
        }
        robot = false;
    }

    @Override
    public void visit(Script script) {
        if (script.getEvent() instanceof Never) {
            // Ignore unconnected blocks
            return;
        }
        visitChildren(script);
    }

    @Override
    public void visit(Event event) {
        if (robot && event instanceof MBlockEvent) {
            realRobot = true;
            robot = false;
            return;
        }
        visit((ASTNode) event);
    }

    @Override
    public void visit(Stmt stmt) {
        if (robot && stmt instanceof MBlockStmt) {
            realRobot = true;
            robot = false;
            return;
        }
        visit((ASTNode) stmt);
    }

    @Override
    public void visit(Expression stmt) {
        if (robot && stmt instanceof MBlockExpr) {
            realRobot = true;
            robot = false;
            return;
        }
        visit((ASTNode) stmt);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
