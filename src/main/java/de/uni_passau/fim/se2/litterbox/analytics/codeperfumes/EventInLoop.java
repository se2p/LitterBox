package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.DistanceTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;


/**
 * A script should execute actions when an event occurs. To ensure correct timing for the action, a continuous check for
 * the event to occur is necessary. So the check must be inside a forever or until loop (and not only in a conditional
 * construct). This is the solution pattern for the bug pattern "Missing Loop Sensing".
 */
public class EventInLoop extends AbstractIssueFinder {

    public static final String NAME = "event_check_in_loop";
    private boolean insideGreenFlagClone = false;
    private boolean insideLoop = false;
    private boolean inCondition = false;

    @Override
    public void visit(Script node) {
        if (ignoreLooseBlocks && node.getEvent() instanceof Never) {
            // Ignore unconnected blocks
            return;
        }
        if (node.getEvent() instanceof GreenFlag || node.getEvent() instanceof StartedAsClone) {
            insideGreenFlagClone = true;
        }
        inCondition = false;
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
    public void visit(UntilStmt node) {
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
    public void visit(IsKeyPressed node) {
        if (insideGreenFlagClone && insideLoop && inCondition) {
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        }
    }

    @Override
    public void visit(Touching node) {
        if (insideGreenFlagClone && insideLoop && inCondition) {
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        }
    }

    @Override
    public void visit(IsMouseDown node) {
        if (insideGreenFlagClone && insideLoop && inCondition) {
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        }
    }

    @Override
    public void visit(ColorTouchingColor node) {
        if (insideGreenFlagClone && insideLoop && inCondition) {
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        }
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        if (insideGreenFlagClone && insideLoop && inCondition) {
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        }
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
    public boolean isDuplicateOf(Issue first, Issue other) {
        if (first == other) {
            return false;
        }
        if (first.getFinder() != other.getFinder()) {
            return false;
        }
        return true;
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
