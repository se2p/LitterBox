/*
 * Copyright (C) 2019 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.ctscore;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueReport;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Evaluates the level of flow control of the Scratch program.
 */
public class FlowControl implements IssueFinder, ScratchVisitor {

    private final int SCRIPT = 1;
    private final int REPEAT_FOREVER = 2;
    private final int UNTIL = 3;
    private String[] notes = new String[4];
    public final static String NAME = "flow_control";
    public final static String SHORT_NAME = "flow";
    private List<String> actorNames = new LinkedList<>();
    private boolean script = false;
    private boolean repeat_forever = false;
    private boolean until = false;


    public FlowControl() {
        notes[0] = "There is a sequence of blocks missing.";
        notes[1] = "Basic level. There is repeat or forever missing.";
        notes[2] = "Developing level. There is repeat until missing.";
        notes[3] = "Proficiency level. Good work!";
    }


    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        actorNames = new LinkedList<>();
        script = false;
        repeat_forever = false;
        until = false;
        program.accept(this);
        int level = 0;
        if (until) {
            level = UNTIL;
        } else if (repeat_forever) {
            level = REPEAT_FOREVER;
        } else if (script) {
            level = SCRIPT;
        }
        return new IssueReport(NAME, level, actorNames, notes[level]);
    }

    @Override
    public void visit(ActorDefinition actor) {
        if (!actor.getChildren().isEmpty()) {
            for (ASTNode child : actor.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(Script node) {
        if ((!(node.getEvent() instanceof Never) && node.getStmtList().getStmts().getListOfStmt().size() > 0)
                || ((node.getEvent() instanceof Never) && node.getStmtList().getStmts().getListOfStmt().size() > 1)) {
            script = true;
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        repeat_forever = true;
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }

    }

    @Override
    public void visit(UntilStmt node) {
        until = true;
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        repeat_forever = true;
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
