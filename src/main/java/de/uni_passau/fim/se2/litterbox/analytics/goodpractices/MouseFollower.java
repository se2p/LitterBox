package de.uni_passau.fim.se2.litterbox.analytics.goodpractices;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GoToPos;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.PointTowards;

/**
 * The sprite follows the mouse in a RepeatForever block.
 */
public class MouseFollower extends AbstractIssueFinder {

    public static final String NAME = "mouse_follower";
    private boolean insideLoop = false;
    private boolean pointToMouse = false;

    @Override
    public void visit(RepeatForeverStmt node) {
        insideLoop = true;
        iterateStmts(node.getStmtList());
        insideLoop = false;
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        insideLoop = true;
        iterateStmts(node.getStmtList());
        insideLoop = false;
    }

    @Override
    public void visit(UntilStmt node) {
        insideLoop = true;
        iterateStmts(node.getStmtList());
        insideLoop = false;
    }

    @Override
    public void visit(GoToPos node) {
        if (insideLoop && node.getPosition() instanceof MousePos) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
        }
        visitChildren(node);
    }

    @Override
    public void visit(PointTowards node) {
        if (insideLoop && node.getPosition() instanceof MousePos) {
            pointToMouse = true;
        }
        visitChildren(node);
    }

    @Override
    public void visit(MoveSteps node) {
        if (insideLoop && pointToMouse) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
        }
        visitChildren(node);
    }

    private void iterateStmts(StmtList node) {
        node.getStmts().forEach(stmt -> {
            if (stmt instanceof GoToPos || stmt instanceof MoveSteps) {
                stmt.accept(this);
                pointToMouse = false;
            } else if (stmt instanceof PointTowards) {
                stmt.accept(this);
            } else {
                pointToMouse = false;
            }
        });
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.GOOD_PRACTICE;
    }
}
