package de.uni_passau.fim.se2.litterbox.analytics.mblock.perfumes;

import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.NumValueVisitor;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.emotion.MovingEmotion;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.MoveDirection;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.MoveSides;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.MoveStop;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.RobotMoveStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.StopOtherScriptsInSprite;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;

public class MotorOffScript extends AbstractRobotFinder {
    private static final String NAME = "motor_off_script";
    private boolean turnsOnMotor;
    private boolean turnsOffMotor;
    private boolean secondRun;

    @Override
    public void visit(ActorDefinition node) {
        turnsOnMotor = false;
        secondRun = false;
        super.visit(node);
        secondRun = true;
        super.visit(node);
    }

    @Override
    public void visit(Script node) {
        turnsOffMotor = false;
        super.visit(node);
        if (secondRun && !node.getStmtList().getStmts().isEmpty()) {
            ASTNode last = node.getStmtList().getStmts().get(node.getStmtList().getStmts().size() - 1);
            if (last instanceof StopAll || last instanceof StopOtherScriptsInSprite) {
                if (!(node.getEvent() instanceof Never) && turnsOnMotor && turnsOffMotor) {
                    addIssue(node.getEvent(), IssueSeverity.MEDIUM);
                }
            }
        }
    }

    @Override
    public void visit(MovingEmotion node) {
        turnsOnMotor = true;
    }

    @Override
    public void visit(RobotMoveStmt node) {
        if (!isZeroPower(node)) {
            turnsOnMotor = true;
        }
        if (secondRun && isZeroPower(node)) {
            turnsOffMotor = true;
        }
    }

    @Override
    public void visit(MoveStop node) {
        if (secondRun) {
            turnsOffMotor = true;
        }
    }

    private boolean isZeroPower(RobotMoveStmt node) {
        NumValueVisitor calc = new NumValueVisitor();
        try {
            if (node instanceof MoveDirection) {
                return 0 == calc.calculateEndValue(((MoveDirection) node).getPercent());
            } else if (node instanceof MoveSides) {
                double left = calc.calculateEndValue(((MoveSides) node).getLeftPower());
                double right = calc.calculateEndValue(((MoveSides) node).getRightPower());
                return left == 0 && right == 0;
            }
        } catch (Exception ignored) {
        }
        return false;
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
