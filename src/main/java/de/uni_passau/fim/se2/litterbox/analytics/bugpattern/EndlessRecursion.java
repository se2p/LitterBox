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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.*;

/**
 * If a custom block calls itself inside its body and has no condition to stop the recursion, it will run for an
 * indefinite amount of time.
 */
public class EndlessRecursion implements IssueFinder, ScratchVisitor {
    public static final String NAME = "endless_recursion";
    public static final String SHORT_NAME = "endlRec";
    public static final String HINT_TEXT = "endless recursion";
    private Set<Issue> issues = new LinkedHashSet<>();
    private ActorDefinition currentActor;
    private Map<LocalIdentifier, ProcedureInfo> procMap;
    private String currentProcedureName;
    private boolean insideProcedure;
    private int loopIfCounter;
    private Program program;

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        program.accept(this);
        return issues;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(ActorDefinition actor) {
        currentActor = actor;
        procMap = program.getProcedureMapping().getProcedures().get(currentActor.getIdent().getName());
        loopIfCounter = 0;
        for (ASTNode child : actor.getChildren()) {
            child.accept(this);
        }
    }

    @Override
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        currentProcedureName = procMap.get(node.getIdent()).getName();
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
        insideProcedure = false;
    }

    @Override
    public void visit(CallStmt node) {
        if (insideProcedure && loopIfCounter == 0) {
            String call = node.getIdent().getName();
            if (call.equals(currentProcedureName)) {
                issues.add(new Issue(this, currentActor, node,
                        HINT_TEXT, node.getMetadata()));
            }
        }
    }

    @Override
    public void visit(IfElseStmt node) {
        loopIfCounter++;
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
        loopIfCounter--;
    }

    @Override
    public void visit(IfThenStmt node) {
        loopIfCounter++;
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
        loopIfCounter--;
    }
}
