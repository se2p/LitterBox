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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.IllegalParameterRefactor;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.type.BooleanType;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ArgumentInfo;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

/**
 * Fix heuristic for the IllegalParameterRefactor bug.
 *
 * <p>The bug is reported when a non-boolean parameter is used in a boolean context.
 * The bug is fixed if the parameter is now properly typed.
 */
public class IllegalParameterRefactorFix extends AbstractFixHeuristic {

    private ArgumentInfo[] currentArguments;
    private boolean insideProcedure;

    private final IllegalParameterRefactor relatedBugFinder = new IllegalParameterRefactor();

    public IllegalParameterRefactorFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected void resetState() {
        currentArguments = null;
        insideProcedure = false;
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        if (procMap.containsKey(node.getIdent())) {
            currentArguments = procMap.get(node.getIdent()).getArguments();
        } else {
            currentArguments = null;
        }
        super.visit(node);
        insideProcedure = false;
    }

    private void checkBool(BoolExpr boolExpr) {
        if (boolExpr instanceof AsBool asBool && asBool.getOperand1() instanceof Parameter ident) {
            if (AstNodeUtil.hasBlockId(ident, getBugLocationBlockId())) {
                boolean isIllegal = false;
                if (currentArguments != null) {
                    for (ArgumentInfo arg : currentArguments) {
                        if (arg.name().equals(ident.getName().getName())
                                && !(arg.type() instanceof BooleanType)) {
                            isIllegal = true;
                            break;
                        }
                    }
                }
                if (!isIllegal) {
                    addFix(ident, ident.getMetadata());
                }
            }
        }
    }

    @Override
    public void visit(IfElseStmt node) {
        if (insideProcedure) {
            checkBool(node.getBoolExpr());
        }
        visitChildren(node);
    }

    @Override
    public void visit(IfThenStmt node) {
        if (insideProcedure) {
            checkBool(node.getBoolExpr());
        }
        visitChildren(node);
    }

    @Override
    public void visit(WaitUntil node) {
        if (insideProcedure) {
            checkBool(node.getUntil());
        }
        visitChildren(node);
    }

    @Override
    public void visit(UntilStmt node) {
        if (insideProcedure) {
            checkBool(node.getBoolExpr());
        }
        visitChildren(node);
    }

    @Override
    public void visit(Not node) {
        if (insideProcedure) {
            checkBool(node.getOperand1());
        }
        visitChildren(node);
    }

    @Override
    public void visit(And node) {
        if (insideProcedure) {
            checkBool(node.getOperand1());
            checkBool(node.getOperand2());
        }
        visitChildren(node);
    }

    @Override
    public void visit(Or node) {
        if (insideProcedure) {
            checkBool(node.getOperand1());
            checkBool(node.getOperand2());
        }
        visitChildren(node);
    }
}
