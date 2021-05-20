package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsKeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;

/**
 * To avoid slower and stuttering movements it is recommended to use a forever loop with a conditional containing a
 * key pressed? expression, followed by a move steps, change x by or change y by statement (or a list of them). This
 * is the solution for the "Stuttering Movement" bug.
 */
public class MovementInLoop extends AbstractIssueFinder {

    public static final String NAME = "movement_in_loop";
    private boolean hasKeyPressed;
    private boolean insideLoop;
    private boolean insideGreenFlagClone;
    private boolean inCondition;
    private boolean subsequentMovement;

    @Override
    public void visit(Script node) {
        if (ignoreLooseBlocks && node.getEvent() instanceof Never) {
            // Ignore unconnected blocks
            return;
        }
        if (node.getEvent() instanceof GreenFlag || node.getEvent() instanceof StartedAsClone) {
            insideGreenFlagClone = true;
        }
        subsequentMovement = false;
        inCondition = false;
        insideLoop = false;
        hasKeyPressed = false;
        super.visit(node);
        insideGreenFlagClone = false;
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        insideLoop = true;
        visitChildren(node);
        insideLoop = false;
    }

    @Override
    public void visit(IfThenStmt node) {
        if (insideGreenFlagClone && insideLoop) {
            inCondition = true;
            BoolExpr boolExpr = node.getBoolExpr();
            boolExpr.accept(this);
            inCondition = false;
        }
        node.getThenStmts().accept(this);
    }

    @Override
    public void visit(IfElseStmt node) {
        if (insideGreenFlagClone && insideLoop) {
            inCondition = true;
            BoolExpr boolExpr = node.getBoolExpr();
            boolExpr.accept(this);
            inCondition = false;
        }
        node.getStmtList().accept(this);
        node.getElseStmts().accept(this);
    }

    @Override
    public void visit(MoveSteps node) {
        if (hasKeyPressed && !subsequentMovement) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
            subsequentMovement = true;
        }
    }

    @Override
    public void visit(ChangeXBy node) {
        if (hasKeyPressed && !subsequentMovement) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
            subsequentMovement = true;
        }
    }

    @Override
    public void visit(ChangeYBy node) {
        if (hasKeyPressed && !subsequentMovement) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
            subsequentMovement = true;
        }
    }

    @Override
    public void visit(TurnRight node) {
        if (hasKeyPressed && !subsequentMovement) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
            subsequentMovement = true;
        }
    }

    @Override
    public void visit(TurnLeft node) {
        if (hasKeyPressed && !subsequentMovement) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
            subsequentMovement = true;
        }
    }

    @Override
    public void visit(IsKeyPressed node) {
        if (insideGreenFlagClone && insideLoop && inCondition) {
            hasKeyPressed = true;
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
