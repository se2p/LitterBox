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

import static de.uni_passau.fim.se2.litterbox.analytics.CommentAdder.addBlockComment;


import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueReport;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
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
    private static final String NOTE1 = "There are no procedures with illegally refactored parameters in your project.";
    private static final String NOTE2 = "Some of the sprites contain procedures with illegally refactored parameters.";
    private boolean found = false;
    private int count = 0;
    private Set<Issue> issues = new LinkedHashSet<>();
    private List<String> actorNames = new LinkedList<>();
    private ActorDefinition currentActor;
    private Map<LocalIdentifier, ProcedureInfo> procedureMap;
    private ArgumentInfo[] currentArguments;
    private boolean insideProcedure;
    private Program program;

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        found = false;
        count = 0;
        actorNames = new LinkedList<>();
        program.accept(this);
        String notes = NOTE1;
        if (count > 0) {
            notes = NOTE2;
        }
        return issues;
        // return new IssueReport(NAME, count, actorNames, notes);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(ActorDefinition actor) {
        currentActor = actor;
        procedureMap = program.getProcedureMapping().getProcedures().get(currentActor.getIdent().getName());
        if (!actor.getChildren().isEmpty()) {
            for (ASTNode child : actor.getChildren()) {
                child.accept(this);
            }
        }

        if (found) {
            found = false;
            actorNames.add(currentActor.getIdent().getName());
        }
    }

    @Override
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        currentArguments = procedureMap.get(node.getIdent()).getArguments();
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
        insideProcedure = false;
    }

    @Override
    public void visit(IfElseStmt node) {
        if (insideProcedure) {
            checkBool(node.getBoolExpr(), node);
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    private void checkBool(BoolExpr boolExpr, AbstractNode node) {
        if (boolExpr instanceof AsBool && ((AsBool) boolExpr).getOperand1() instanceof Parameter) {
           Parameter ident = (Parameter) ((AsBool) boolExpr).getOperand1();

                for (ArgumentInfo currentArgument : currentArguments) {
                    if (currentArgument.getName().equals(ident.getName().getName()) && !(currentArgument.getType() instanceof BooleanType)) {
                        found = true;
                        count++;
                        issues.add(new Issue(this, currentActor, node)); // TODO: boolExpr?
                        addBlockComment((NonDataBlockMetadata) ident.getMetadata(), currentActor,
                                HINT_TEXT, SHORT_NAME + count);
                    }
                }

        }
    }

    @Override
    public void visit(IfThenStmt node) {
        if (insideProcedure) {
            checkBool(node.getBoolExpr(), node);
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(WaitUntil node) {
        if (insideProcedure) {
            checkBool(node.getUntil(), node);
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(UntilStmt node) {
        if (insideProcedure) {
            checkBool(node.getBoolExpr(), node);
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(Not node) {
        if (insideProcedure) {
            checkBool(node.getOperand1(), node);
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(And node) {
        if (insideProcedure) {
            checkBool(node.getOperand1(), node);
            checkBool(node.getOperand2(), node);
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(Or node) {
        if (insideProcedure) {
            checkBool(node.getOperand1(), node);
            checkBool(node.getOperand2(), node);
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }
}
