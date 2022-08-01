package de.uni_passau.fim.se2.litterbox.analytics.mblock.perfumes;

import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.BinaryExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.num.Potentiometer;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;

public class GearPotentiometerInBounds extends AbstractRobotFinder {
    private static final String NAME = "gear_potentiometer_in_bounds";
    private static final int GEAR_MAX = 100;
    private static final int GEAR_MIN = 0;
    private boolean insideComparison;
    private boolean hasGear;
    private double sensorValue;
    private boolean setValue;
    private boolean firstHasGear;
    private boolean secondHasGear;
    private boolean visitFirst;
    private boolean visitSecond;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(BiggerThan node) {
        visitComp(node);
        if (setValue && hasGear) {
            if (((firstHasGear && sensorValue >= GEAR_MIN && sensorValue < GEAR_MAX)
                    || (secondHasGear && sensorValue <= GEAR_MAX && sensorValue > GEAR_MIN)) && (sensorValue % 1) == 0) {
                addIssue(node, IssueSeverity.LOW);
            }
        }
        insideComparison = false;
        hasGear = false;
        firstHasGear = false;
        secondHasGear = false;
    }

    @Override
    public void visit(LessThan node) {
        visitComp(node);
        if (setValue && hasGear) {
            if (((firstHasGear && sensorValue <= GEAR_MAX && sensorValue > GEAR_MIN)
                    || (secondHasGear && sensorValue >= GEAR_MIN && sensorValue < GEAR_MAX)) && (sensorValue % 1) == 0) {
                addIssue(node, IssueSeverity.LOW);
            }
        }
        insideComparison = false;
        hasGear = false;
        firstHasGear = false;
        secondHasGear = false;
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
    public void visit(Potentiometer node) {
        if (insideComparison) {
            hasGear = true;
        }
        if (visitFirst) {
            firstHasGear = true;
        } else if (visitSecond) {
            secondHasGear = true;
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
        return IssueType.PERFUME;
    }
}
