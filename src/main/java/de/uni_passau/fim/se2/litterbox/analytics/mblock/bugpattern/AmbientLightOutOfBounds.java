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
package de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.RobotCode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.BinaryExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.num.AmbientLight;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.num.DetectAmbientLight;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.num.DetectAmbientLightPort;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;

public class AmbientLightOutOfBounds extends AbstractRobotFinder {
    private static final String NAME = "ambient_light_out_of_bounds";
    private static final int AMBIENT_LIGHT_MAX_CODEY = 100;
    private static final int AMBIENT_LIGHT_MAX_MBOT = 1020;
    private static final int AMBIENT_LIGHT_MIN = 0;
    private boolean insideComparison;
    private boolean hasAmbientLight;
    private double sensorValue;
    private boolean setValue;
    private boolean firstHasAmbient;
    private boolean secondHasAmbient;
    private boolean visitFirst;
    private boolean visitSecond;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(Equals node) {
        insideComparison = true;
        setValue = false;
        visitChildren(node);
        if (setValue && hasAmbientLight) {
            if (robot == RobotCode.CODEY && (sensorValue > AMBIENT_LIGHT_MAX_CODEY || sensorValue < AMBIENT_LIGHT_MIN)) {
                addIssue(node, IssueSeverity.LOW);
            } else if (robot == RobotCode.MCORE && (sensorValue > AMBIENT_LIGHT_MAX_MBOT || sensorValue < AMBIENT_LIGHT_MIN)) {
                addIssue(node, IssueSeverity.LOW);
            }
        }
        insideComparison = false;
        hasAmbientLight = false;
    }

    @Override
    public void visit(BiggerThan node) {
        visitComp(node);
        if (setValue && hasAmbientLight) {
            if (robot == RobotCode.CODEY && ((firstHasAmbient && sensorValue >= AMBIENT_LIGHT_MAX_CODEY) || (secondHasAmbient && sensorValue <= AMBIENT_LIGHT_MIN))) {
                addIssue(node, IssueSeverity.LOW);
            } else if (robot == RobotCode.MCORE && ((firstHasAmbient && sensorValue >= AMBIENT_LIGHT_MAX_MBOT) || (secondHasAmbient && sensorValue <= AMBIENT_LIGHT_MIN))) {
                addIssue(node, IssueSeverity.LOW);
            }
        }
        insideComparison = false;
        hasAmbientLight = false;
        firstHasAmbient = false;
        secondHasAmbient = false;
    }

    @Override
    public void visit(LessThan node) {
        visitComp(node);
        if (setValue && hasAmbientLight) {
            if (robot == RobotCode.CODEY && ((firstHasAmbient && sensorValue <= AMBIENT_LIGHT_MIN) || (secondHasAmbient && sensorValue >= AMBIENT_LIGHT_MAX_CODEY))) {
                addIssue(node, IssueSeverity.LOW);
            } else if (robot == RobotCode.MCORE && ((firstHasAmbient && sensorValue <= AMBIENT_LIGHT_MIN) || (secondHasAmbient && sensorValue >= AMBIENT_LIGHT_MAX_MBOT))) {
                addIssue(node, IssueSeverity.LOW);
            }
        }
        insideComparison = false;
        hasAmbientLight = false;
        firstHasAmbient = false;
        secondHasAmbient = false;
    }

    private void visitComp(BinaryExpression node) {
        insideComparison = true;
        setValue = false;
        visitFirst = true;
        node.getOperand1().accept(this);
        visitFirst = false;
        visitSecond = true;
        node.getOperand2().accept(this);
        visitSecond = false;
    }

    @Override
    public void visit(AmbientLight node) {
        if (insideComparison) {
            hasAmbientLight = true;
        }
        if (visitFirst) {
            firstHasAmbient = true;
        } else if (visitSecond) {
            secondHasAmbient = true;
        }
    }

    @Override
    public void visit(DetectAmbientLight node) {
        if (insideComparison) {
            hasAmbientLight = true;
        }
        if (visitFirst) {
            firstHasAmbient = true;
        } else if (visitSecond) {
            secondHasAmbient = true;
        }
    }

    @Override
    public void visit(DetectAmbientLightPort node) {
        if (insideComparison) {
            hasAmbientLight = true;
        }
        if (visitFirst) {
            firstHasAmbient = true;
        } else if (visitSecond) {
            secondHasAmbient = true;
        }
    }

    @Override
    public void visit(NumberLiteral node) {
        if (insideComparison && !setValue) {
            setValue = true;
            sensorValue = node.getValue();
        }
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }
}
