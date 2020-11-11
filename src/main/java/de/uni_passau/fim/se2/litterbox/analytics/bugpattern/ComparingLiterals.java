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
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Not;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Reporter blocks are used to evaluate the truth value of certain expressions.
 * Not only is it possible to compare literals to variables or the results of other reporter blocks, literals can
 * also be compared to literals.
 * Since this will lead to the same result in each execution this construct is unnecessary and can obscure the fact
 * that certain blocks will never or always be executed.
 */
public class ComparingLiterals extends AbstractIssueFinder {

    public static final String NAME = "comparing_literals";
    public static final String DEFAULT_TRUE = "comparing_literals_default_true";
    public static final String DEFAULT_FALSE = "comparing_literals_default_false";
    public static final String DEFAULT_VARIABLE = "comparing_literals_default_variable";
    public static final String DEFAULT_VARIABLE_EXISTS = "comparing_literals_default_variable_exists";
    public static final String WAIT_TRUE = "comparing_literals_wait_true";
    public static final String WAIT_FALSE = "comparing_literals_wait_false";
    public static final String WAIT_VARIABLE = "comparing_literals_wait_variable";
    public static final String WAIT_VARIABLE_EXISTS = "comparing_literals_wait_variable_exists";
    public static final String HINT_TRUE_FALSE = "TRUEFALSE";
    public static final String ALWAYS_NEVER = "ALWAYSNEVER";
    public static final String ALWAYS = "always";
    public static final String NEVER = "never";
    public static final String ALWAYS_OR_NEVER = "always_or_never";
    private boolean inWait;

    @Override
    public void visit(Equals node) {
        if ((node.getOperand1() instanceof StringLiteral || node.getOperand1() instanceof NumberLiteral)
                && (node.getOperand2() instanceof StringLiteral || node.getOperand2() instanceof NumberLiteral)) {
            Hint hint;
            ASTNode parent = node.getParentNode();
            boolean isTopLevelCond = checkIfTopCond(parent);
            boolean isNot = checkIfNotIsTop(parent);
            if (node.getOperand1() instanceof NumberLiteral && node.getOperand2() instanceof NumberLiteral) {
                double text1 = ((NumberLiteral) node.getOperand1()).getValue();
                double text2 = ((NumberLiteral) node.getOperand2()).getValue();
                hint = generateHint(text1 == text2, inWait, isTopLevelCond, isNot, false, node);
            } else if (node.getOperand1() instanceof StringLiteral && node.getOperand2() instanceof StringLiteral) {
                String text1 = getLiteralValue(node.getOperand1());
                String text2 = getLiteralValue(node.getOperand2());

                int result = text1.compareTo(text2);
                hint = generateHint(result == 0, inWait, isTopLevelCond, isNot, false, node);
            } else {
                String text1 = getLiteralValue(node.getOperand1());
                String text2 = getLiteralValue(node.getOperand2());

                int result = text1.compareTo(text2);
                hint = generateHint(result == 0, inWait, isTopLevelCond, isNot, true, node);
                hint.setParameter(Hint.HINT_VARIABLE, "\"" + possibleVariableName(node.getOperand1(), node.getOperand2()) + "\"");
            }

            addIssue(node, node.getMetadata(), hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(WaitUntil node) {
        inWait = true;
        super.visit(node);
        inWait = false;
    }

    private boolean checkIfNotIsTop(ASTNode parent) {
        return parent instanceof Not && checkIfTopCond(parent.getParentNode());
    }

    private boolean checkIfTopCond(ASTNode parent) {
        return parent instanceof IfThenStmt || parent instanceof IfElseStmt || parent instanceof UntilStmt || parent instanceof WaitUntil;
    }

    private String possibleVariableName(ComparableExpr node1, ComparableExpr node2) {
        if (node1 instanceof StringLiteral) {
            return ((StringLiteral) node1).getText();
        } else {
            return ((StringLiteral) node2).getText();
        }
    }

    private String getLiteralValue(ComparableExpr node) {
        String text1;
        if (node instanceof StringLiteral) {
            text1 = ((StringLiteral) node).getText();
        } else {
            text1 = String.valueOf(((NumberLiteral) node).getValue());
        }
        return text1;
    }

    private Hint generateHint(boolean value, boolean wait, boolean top, boolean topNot, boolean variable, ASTNode currentNode) {
        Hint hint;
        boolean variableExits = false;
        if (variable) {
            variableExits = checkForVariableAsLiteral(currentNode);
        }
        if (wait) {
            if (variableExits) {
                hint = getWaitHint(value, variable, WAIT_VARIABLE_EXISTS, WAIT_TRUE, WAIT_FALSE);
            } else {
                hint = getWaitHint(value, variable, WAIT_VARIABLE, WAIT_TRUE, WAIT_FALSE);
            }
        } else {
            if (variableExits) {
                hint = getWaitHint(value, variable, DEFAULT_VARIABLE_EXISTS, DEFAULT_TRUE, DEFAULT_FALSE);
            } else {
                hint = getWaitHint(value, variable, DEFAULT_VARIABLE, DEFAULT_TRUE, DEFAULT_FALSE);
            }
        }
        if (top) {
            if (value) {
                hint.setParameter(ALWAYS_NEVER, IssueTranslator.getInstance().getInfo(ALWAYS));
            } else {
                hint.setParameter(ALWAYS_NEVER, IssueTranslator.getInstance().getInfo(NEVER));
            }
        } else if (topNot) {
            if (!value) {
                hint.setParameter(ALWAYS_NEVER, IssueTranslator.getInstance().getInfo(ALWAYS));
            } else {
                hint.setParameter(ALWAYS_NEVER, IssueTranslator.getInstance().getInfo(NEVER));
            }
        } else {
            hint.setParameter(ALWAYS_NEVER, IssueTranslator.getInstance().getInfo(ALWAYS_OR_NEVER));
        }
        return hint;
    }

    private Hint getWaitHint(boolean value, boolean variable, String waitVariable, String waitTrue, String waitFalse) {
        Hint hint;
        if (variable) {
            hint = new Hint(waitVariable);
            hint.setParameter(HINT_TRUE_FALSE, IssueTranslator.getInstance().getInfo(String.valueOf(value)));
        } else {
            if (value) {
                hint = new Hint(waitTrue);
            } else {
                hint = new Hint(waitFalse);
            }
        }
        return hint;
    }

    private boolean checkForVariableAsLiteral(ASTNode currentNode) {
        VariableAsLiteral finder = new VariableAsLiteral();
        Set<Issue> variablesAsLiterals = finder.check(program);
        for (Issue issue : variablesAsLiterals) {
            if (issue.getActor() == currentActor && issue.getCodeLocation() == currentNode && issue.getProcedure() == currentProcedure && issue.getScript() == currentScript) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void visit(LessThan node) {
        if ((node.getOperand1() instanceof StringLiteral || node.getOperand1() instanceof NumberLiteral)
                && (node.getOperand2() instanceof StringLiteral || node.getOperand2() instanceof NumberLiteral)) {
            Hint hint;
            ASTNode parent = node.getParentNode();
            boolean isTopLevelCond = checkIfTopCond(parent);
            boolean isNot = checkIfNotIsTop(parent);
            if (node.getOperand1() instanceof NumberLiteral && node.getOperand2() instanceof NumberLiteral) {
                double text1 = ((NumberLiteral) node.getOperand1()).getValue();
                double text2 = ((NumberLiteral) node.getOperand2()).getValue();
                hint = generateHint(text1 < text2, inWait, isTopLevelCond, isNot, false, node);
            } else if (node.getOperand1() instanceof StringLiteral && node.getOperand2() instanceof StringLiteral) {
                String text1 = getLiteralValue(node.getOperand1());
                String text2 = getLiteralValue(node.getOperand2());
                int result = text1.compareTo(text2);
                hint = generateHint(result < 0, inWait, isTopLevelCond, isNot, false, node);
            } else {
                String text1 = getLiteralValue(node.getOperand1());
                String text2 = getLiteralValue(node.getOperand2());
                int result = text1.compareTo(text2);
                hint = generateHint(result < 0, inWait, isTopLevelCond, isNot, true, node);
                hint.setParameter(Hint.HINT_VARIABLE, "\"" + possibleVariableName(node.getOperand1(), node.getOperand2()) + "\"");
            }
            addIssue(node, node.getMetadata(), hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(BiggerThan node) {
        if ((node.getOperand1() instanceof StringLiteral || node.getOperand1() instanceof NumberLiteral)
                && (node.getOperand2() instanceof StringLiteral || node.getOperand2() instanceof NumberLiteral)) {
            Hint hint;
            ASTNode parent = node.getParentNode();
            boolean isTopLevelCond = checkIfTopCond(parent);
            boolean isNot = checkIfNotIsTop(parent);
            if (node.getOperand1() instanceof NumberLiteral && node.getOperand2() instanceof NumberLiteral) {
                double text1 = ((NumberLiteral) node.getOperand1()).getValue();
                double text2 = ((NumberLiteral) node.getOperand2()).getValue();
                hint = generateHint(text1 > text2, inWait, isTopLevelCond, isNot, false, node);
            } else if (node.getOperand1() instanceof StringLiteral && node.getOperand2() instanceof StringLiteral) {
                String text1 = getLiteralValue(node.getOperand1());
                String text2 = getLiteralValue(node.getOperand2());
                int result = text1.compareTo(text2);
                hint = generateHint(result > 0, inWait, isTopLevelCond, isNot, false, node);
            } else {
                String text1 = getLiteralValue(node.getOperand1());
                String text2 = getLiteralValue(node.getOperand2());

                int result = text1.compareTo(text2);
                hint = generateHint(result > 0, inWait, isTopLevelCond, isNot, true, node);
                hint.setParameter(Hint.HINT_VARIABLE, "\"" + possibleVariableName(node.getOperand1(), node.getOperand2()) + "\"");
            }
            addIssue(node, node.getMetadata(), hint);
        }
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    @Override
    public Collection<String> getHintKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(DEFAULT_FALSE);
        keys.add(DEFAULT_TRUE);
        keys.add(DEFAULT_VARIABLE);
        keys.add(DEFAULT_VARIABLE_EXISTS);
        keys.add(WAIT_FALSE);
        keys.add(WAIT_TRUE);
        keys.add(WAIT_VARIABLE);
        keys.add(WAIT_VARIABLE_EXISTS);
        return keys;
    }
}
