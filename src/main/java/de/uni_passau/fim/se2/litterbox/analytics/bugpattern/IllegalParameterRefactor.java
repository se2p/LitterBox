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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.type.BooleanType;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ArgumentInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.*;

public class IllegalParameterRefactor implements IssueFinder, ScratchVisitor {
    public static final String NAME = "illegal_parameter_refactor";
    public static final String SHORT_NAME = "illParamRefac";
    public static final String HINT_TEXT = "illegal parameter refactor";
    private Set<Issue> issues = new LinkedHashSet<>();
    private ActorDefinition currentActor;
    private Map<LocalIdentifier, ProcedureInfo> procedureMap;
    private ArgumentInfo[] currentArguments;
    private boolean insideProcedure;
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
        procedureMap = program.getProcedureMapping().getProcedures().get(currentActor.getIdent().getName());
        for (ASTNode child : actor.getChildren()) {
            child.accept(this);
        }
    }

    @Override
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        currentArguments = procedureMap.get(node.getIdent()).getArguments();
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
        insideProcedure = false;
    }

    @Override
    public void visit(IfElseStmt node) {
        if (insideProcedure) {
            checkBool(node.getBoolExpr());
        }
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
    }

    private void checkBool(BoolExpr boolExpr) {
        if (boolExpr instanceof AsBool && ((AsBool) boolExpr).getOperand1() instanceof Parameter) {
           Parameter ident = (Parameter) ((AsBool) boolExpr).getOperand1();

                for (ArgumentInfo currentArgument : currentArguments) {
                    if (currentArgument.getName().equals(ident.getName().getName()) && !(currentArgument.getType() instanceof BooleanType)) {
                        issues.add(new Issue(this, currentActor, ident,
                                HINT_TEXT, ident.getMetadata()));
                    }
                }

        }
    }

    @Override
    public void visit(IfThenStmt node) {
        if (insideProcedure) {
            checkBool(node.getBoolExpr());
        }
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
    }

    @Override
    public void visit(WaitUntil node) {
        if (insideProcedure) {
            checkBool(node.getUntil());
        }
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
    }

    @Override
    public void visit(UntilStmt node) {
        if (insideProcedure) {
            checkBool(node.getBoolExpr());
        }
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
    }

    @Override
    public void visit(Not node) {
        if (insideProcedure) {
            checkBool(node.getOperand1());
        }
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
    }

    @Override
    public void visit(And node) {
        if (insideProcedure) {
            checkBool(node.getOperand1());
            checkBool(node.getOperand2());
        }
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
    }

    @Override
    public void visit(Or node) {
        if (insideProcedure) {
            checkBool(node.getOperand1());
            checkBool(node.getOperand2());
        }
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
    }
}
