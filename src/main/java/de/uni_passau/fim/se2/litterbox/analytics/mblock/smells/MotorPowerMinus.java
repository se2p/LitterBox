package de.uni_passau.fim.se2.litterbox.analytics.mblock.smells;

import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.NumValueVisitor;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.MoveSides;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.RobotMoveStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.SinglePower;

public class MotorPowerMinus extends AbstractRobotFinder {

    private static final String NAME = "motor_power_minus";
    private static final double MCORE_MIN_VALUE = 0;

    @Override
    public void visit(Script script) {
        ignoreLooseBlocks = true;
        super.visit(script);
    }

    @Override
    public void visit(RobotMoveStmt node) {
        NumValueVisitor calc = new NumValueVisitor();
        try {
            if (node instanceof MoveSides) {
                NumExpr powerLeft = ((MoveSides) node).getLeftPower();
                NumExpr powerRight = ((MoveSides) node).getRightPower();
                double powerLeftValue = calc.calculateEndValue(powerLeft);
                double powerRightValue = calc.calculateEndValue(powerRight);
                if (powerLeftValue < MCORE_MIN_VALUE || powerRightValue < MCORE_MIN_VALUE) {
                    addIssue(node);
                }
            } else if (node instanceof SinglePower) {
                NumExpr power = ((SinglePower) node).getPercent();
                double powerValue = calc.calculateEndValue(power);
                if (powerValue < MCORE_MIN_VALUE) {
                    addIssue(node);
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }
}
