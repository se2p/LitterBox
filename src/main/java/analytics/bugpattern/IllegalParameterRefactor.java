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
package analytics.bugpattern;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import analytics.IssueFinder;
import analytics.IssueReport;
import ast.Constants;
import ast.model.ASTNode;
import ast.model.ActorDefinition;
import ast.model.Program;
import ast.model.expression.bool.And;
import ast.model.expression.bool.BoolExpr;
import ast.model.expression.bool.Not;
import ast.model.expression.bool.Or;
import ast.model.procedure.ProcedureDefinition;
import ast.model.statement.common.WaitUntil;
import ast.model.statement.control.IfElseStmt;
import ast.model.statement.control.IfThenStmt;
import ast.model.statement.control.UntilStmt;
import ast.model.type.BooleanType;
import ast.model.variable.Identifier;
import ast.parser.symboltable.ArgumentInfo;
import ast.parser.symboltable.ProcedureInfo;
import ast.visitor.ScratchVisitor;
import utils.Preconditions;

public class IllegalParameterRefactor implements IssueFinder, ScratchVisitor {
    public static final String NAME = "illegal_parameter_refactor";
    public static final String SHORT_NAME = "illParamRefac";
    private static final String NOTE1 = "There are no procedures with illegally refactored parameters in your project.";
    private static final String NOTE2 = "Some of the sprites contain procedures with illegally refactored parameters.";
    private boolean found = false;
    private int count = 0;
    private List<String> actorNames = new LinkedList<>();
    private ActorDefinition currentActor;
    private Map<Identifier, ProcedureInfo> procedureMap;
    private ArgumentInfo[] currentArguments;
    private boolean insideProcedure;
    private Program program;

    @Override
    public IssueReport check(Program program) {
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
        return new IssueReport(NAME, count, actorNames, notes);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(ActorDefinition actor) {
        currentActor = actor;
        procedureMap=program.getProcedureMapping().getProcedures().get(currentActor.getIdent().getName());
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
            checkBool(node.getBoolExpr());
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    private void checkBool(BoolExpr boolExpr) {
        if (boolExpr instanceof Identifier) {
            Identifier ident = (Identifier) boolExpr;
            if (ident.getName().startsWith(Constants.PARAMETER_ABBREVIATION)) {
                for (ArgumentInfo currentArgument : currentArguments) {
                    if (currentArgument.getName().equals(ident.getName()) && !(currentArgument.getType() instanceof BooleanType)) {
                        found = true;
                        count++;
                    }
                }
            }
        }
    }

    @Override
    public void visit(IfThenStmt node) {
        if (insideProcedure) {
            checkBool(node.getBoolExpr());
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
            checkBool(node.getUntil());
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
            checkBool(node.getBoolExpr());
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
            checkBool(node.getOperand1());
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
            checkBool(node.getOperand1());
            checkBool(node.getOperand2());
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
            checkBool(node.getOperand1());
            checkBool(node.getOperand2());
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }
}
