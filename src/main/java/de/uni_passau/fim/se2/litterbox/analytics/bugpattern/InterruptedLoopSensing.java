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

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.DistanceTo;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.ItemOfVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ThinkForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsToXY;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

/**
 * A sensing in a control structure can be interrupted if the control body has a stmt that takes a longer time like gliding.
 */
public class InterruptedLoopSensing extends AbstractIssueFinder {
    private final String NAME = "interrupted_loop_sensing";
    private boolean inCondition = false;
    private boolean insideEquals = false;
    private boolean sensingCollision = false;
    private boolean sensingOther = false;
    private boolean insideForever = false;
    private boolean insideControl = false;
    private String blockName;
    private ASTNode variableName;
    private boolean checkingVariable;

    @Override
    public void visit(RepeatForeverStmt node) {
        if (!checkingVariable) {
            insideForever = true;
            visitChildren(node);
            insideForever = false;
        }
    }

    @Override
    public void visit(UntilStmt node) {
        if (!checkingVariable) {
            inCondition = true;
            node.getBoolExpr().accept(this);
            if (variableName != null) {
                checkForVariableChange(node.getStmtList());
            }
            inCondition = false;
            insideControl = true;
            blockName = IssueTranslator.getInstance().getInfo("until");
            node.getStmtList().accept(this);
            insideControl = false;
            sensingCollision = false;
            sensingOther = false;
        }
    }

    @Override
    public void visit(IfElseStmt node) {
        if (!checkingVariable) {
            if (insideForever) {
                inCondition = true;
                node.getBoolExpr().accept(this);
                if (variableName != null) {
                    checkForVariableChange(node.getStmtList());
                    checkForVariableChange(node.getElseStmts());
                }
                inCondition = false;
                insideControl = true;
                blockName = IssueTranslator.getInstance().getInfo("if") + " " + IssueTranslator.getInstance().getInfo("then") + " " + IssueTranslator.getInstance().getInfo("else");
                node.getStmtList().accept(this);
                node.getElseStmts().accept(this);
                insideControl = false;
                sensingCollision = false;
                sensingOther = false;
            }
        }
    }

    @Override
    public void visit(IfThenStmt node) {
        if (!checkingVariable) {
            if (insideForever) {
                inCondition = true;
                node.getBoolExpr().accept(this);
                if (variableName != null) {
                    checkForVariableChange(node.getThenStmts());
                }
                inCondition = false;
                insideControl = true;
                blockName = IssueTranslator.getInstance().getInfo("if") + " " + IssueTranslator.getInstance().getInfo("then");
                node.getThenStmts().accept(this);
                insideControl = false;
                sensingCollision = false;
                sensingOther = false;
            }
        }
    }

    /**
     * If the variable is changed inside these stmts, it should not trigger the finder, as the insides of the loop are responsible for the exit condition.
     *
     * @param stmts stmts that should be searched
     */
    private void checkForVariableChange(StmtList stmts) {
        checkingVariable = true;
        stmts.accept(this);
        checkingVariable = false;
        variableName = null;
    }

    @Override
    public void visit(SetVariableTo node) {
        if (checkingVariable) {
            if (node.getIdentifier().equals(variableName)) {
                sensingOther = false;
            }
        }
    }

    @Override
    public void visit(ChangeVariableBy node) {
        if (checkingVariable) {
            if (node.getIdentifier().equals(variableName)) {
                sensingOther = false;
            }
        }
    }

    @Override
    public void visit(AddTo node) {
        if (checkingVariable) {
            if (node.getIdentifier().equals(variableName)) {
                sensingOther = false;
            }
        }
    }

    @Override
    public void visit(DeleteOf node) {
        if (checkingVariable) {
            if (node.getIdentifier().equals(variableName)) {
                sensingOther = false;
            }
        }
    }

    @Override
    public void visit(DeleteAllOf node) {
        if (checkingVariable) {
            if (node.getIdentifier().equals(variableName)) {
                sensingOther = false;
            }
        }
    }

    @Override
    public void visit(InsertAt node) {
        if (checkingVariable) {
            if (node.getIdentifier().equals(variableName)) {
                sensingOther = false;
            }
        }
    }

    @Override
    public void visit(ReplaceItem node) {
        if (checkingVariable) {
            if (node.getIdentifier().equals(variableName)) {
                sensingOther = false;
            }
        }
    }

    @Override
    public void visit(GlideSecsTo node) {
        if (!checkingVariable) {
            if (insideControl && (sensingCollision || sensingOther)) {
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.THEN_ELSE, blockName);
                hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("glide_secs_to"));
                addIssue(node, node.getMetadata(), hint);
            }
        }
    }

    @Override
    public void visit(GlideSecsToXY node) {
        if (!checkingVariable) {
            if (insideControl && (sensingCollision || sensingOther)) {
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.THEN_ELSE, blockName);
                hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("glide_secs_to_xy"));
                addIssue(node, node.getMetadata(), hint);
            }
        }
    }

    @Override
    public void visit(WaitSeconds node) {
        if (!checkingVariable) {
            if (insideControl && sensingOther) {
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.THEN_ELSE, blockName);
                hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("wait_seconds"));
                addIssue(node, node.getMetadata(), hint);
            }
        }
    }

    @Override
    public void visit(ThinkForSecs node) {
        if (!checkingVariable) {
            if (insideControl && sensingOther) {
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.THEN_ELSE, blockName);
                hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("think_seconds"));
                addIssue(node, node.getMetadata(), hint);
            }
        }
    }

    @Override
    public void visit(SayForSecs node) {
        if (!checkingVariable) {
            if (insideControl && sensingOther) {
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.THEN_ELSE, blockName);
                hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("say_seconds"));
                addIssue(node, node.getMetadata(), hint);
            }
        }
    }

    @Override
    public void visit(IsKeyPressed node) {
        if (!checkingVariable) {
            if (inCondition) {
                sensingOther = true;
            }
        }
    }

    @Override
    public void visit(Touching node) {
        if (!checkingVariable) {
            if (inCondition) {
                sensingCollision = true;
            }
        }
    }

    @Override
    public void visit(IsMouseDown node) {
        if (!checkingVariable) {
            if (inCondition) {
                sensingOther = true;
            }
        }
    }

    @Override
    public void visit(ColorTouchingColor node) {
        if (!checkingVariable) {
            if (inCondition) {
                sensingCollision = true;
            }
        }
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        if (!checkingVariable) {
            if (inCondition) {
                sensingCollision = true;
            }
        }
    }

    @Override
    public void visit(DistanceTo node) {
        if (!checkingVariable) {
            if (inCondition) {
                sensingCollision = true;
            }
        }
    }

    @Override
    public void visit(Equals node) {
        if (!checkingVariable) {
            if (inCondition) {
                insideEquals = true;
            }
            visitChildren(node);
            insideEquals = false;
        }
    }

    @Override
    public void visit(Variable node) {
        if (!checkingVariable) {
            if (insideEquals) {
                sensingOther = true;
                variableName = node.getParentNode();
            }
        }
    }

    @Override
    public void visit(ItemOfVariable node) {
        if (!checkingVariable) {
            if (insideEquals) {
                sensingOther = true;
                variableName = node.getIdentifier();
            }
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }
}
