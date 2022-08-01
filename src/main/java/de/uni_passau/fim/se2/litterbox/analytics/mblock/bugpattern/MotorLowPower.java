package de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.NumValueVisitor;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.MoveSides;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.RobotMoveStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.SinglePower;

import static de.uni_passau.fim.se2.litterbox.analytics.mblock.RobotCode.MCORE;
import static java.lang.Math.abs;

public class MotorLowPower extends AbstractRobotFinder {
    private static final double MOTOR_MIN_VALUE = 25;
    private static final String NAME = "motor_low_power";

    @Override
    public void visit(Script script) {
        ignoreLooseBlocks = true;
        super.visit(script);
    }

    @Override
    public void visit(RobotMoveStmt node) {
        if (robot == MCORE) {
            NumValueVisitor calc = new NumValueVisitor();
            try {
                if (node instanceof MoveSides) {
                    NumExpr powerLeft = ((MoveSides) node).getLeftPower();
                    NumExpr powerRight = ((MoveSides) node).getRightPower();
                    double powerLeftValue = abs(calc.calculateEndValue(powerLeft));
                    double powerRightValue = abs(calc.calculateEndValue(powerRight));
                    if (powerLeftValue < MOTOR_MIN_VALUE && powerLeftValue != 0 || powerRightValue < MOTOR_MIN_VALUE && powerRightValue != 0) {
                        addIssue(node, IssueSeverity.MEDIUM);
                    }
                } else if (node instanceof SinglePower) {
                    NumExpr power = ((SinglePower) node).getPercent();
                    double powerValue = abs(calc.calculateEndValue(power));
                    if (powerValue < MOTOR_MIN_VALUE && powerValue != 0) {
                        addIssue(node, IssueSeverity.MEDIUM);
                    }
                }
            } catch (Exception ignored) {
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
