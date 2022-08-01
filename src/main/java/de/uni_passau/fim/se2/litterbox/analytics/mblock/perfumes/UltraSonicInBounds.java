package de.uni_passau.fim.se2.litterbox.analytics.mblock.perfumes;

import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.num.DetectDistancePort;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;

public class UltraSonicInBounds extends AbstractRobotFinder {
    private static final String NAME = "ultra_sonic_in_bounds";
    private static final double ULTRASONIC_MAX_VALUE = 400;
    private static final double ULTRASONIC_MIN_VALUE = 3;
    private double sensorValue;
    private boolean setValue;
    private boolean firstIsUltraSensor;
    private boolean secondIsUltraSensor;
    private boolean visitFirst;
    private boolean visitSecond;

    @Override
    public void visit(LessThan node) {
        setValue = false;
        visitFirst = true;
        node.getOperand1().accept(this);
        visitFirst = false;
        visitSecond = true;
        node.getOperand2().accept(this);
        visitSecond = false;
        if (setValue) {
            if (firstIsUltraSensor && !secondIsUltraSensor) {
                if (sensorValue <= ULTRASONIC_MAX_VALUE && sensorValue > ULTRASONIC_MIN_VALUE) {
                    addIssue(node, IssueSeverity.LOW);
                }
            } else if (!firstIsUltraSensor && secondIsUltraSensor) {
                if (sensorValue >= ULTRASONIC_MIN_VALUE && sensorValue < ULTRASONIC_MAX_VALUE) {
                    addIssue(node, IssueSeverity.LOW);
                }
            }
        }
        firstIsUltraSensor = false;
        secondIsUltraSensor = false;
    }

    @Override
    public void visit(BiggerThan node) {
        setValue = false;
        visitFirst = true;
        node.getOperand1().accept(this);
        visitFirst = false;
        visitSecond = true;
        node.getOperand2().accept(this);
        visitSecond = false;
        if (setValue) {
            if (firstIsUltraSensor && !secondIsUltraSensor) {
                if (sensorValue >= ULTRASONIC_MIN_VALUE && sensorValue < ULTRASONIC_MAX_VALUE) {
                    addIssue(node, IssueSeverity.LOW);
                }
            } else if (!firstIsUltraSensor && secondIsUltraSensor) {
                if (sensorValue <= ULTRASONIC_MAX_VALUE && sensorValue > ULTRASONIC_MIN_VALUE) {
                    addIssue(node, IssueSeverity.LOW);
                }
            }
        }
        firstIsUltraSensor = false;
        secondIsUltraSensor = false;
    }

    @Override
    public void visit(NumberLiteral node) {
        if (!setValue) {
            sensorValue = node.getValue();
            setValue = true;
        }
    }

    @Override
    public void visit(DetectDistancePort node) {
        if (visitFirst) {
            firstIsUltraSensor = true;
        } else if (visitSecond) {
            secondIsUltraSensor = true;
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.PERFUME;
    }
}
