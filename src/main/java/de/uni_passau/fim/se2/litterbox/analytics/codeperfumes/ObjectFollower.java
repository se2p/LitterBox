package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GoToPos;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.PointTowards;

import java.util.List;

/**
 * The sprite follows another object (another sprite).
 */
public class ObjectFollower extends AbstractIssueFinder {

    public static final String NAME = "object_follower";
    private boolean insideLoop = false;
    private boolean pointToObject = false;

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
        if (insideLoop && node.getPosition() instanceof FromExpression) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
        }
    }

    @Override
    public void visit(PointTowards node) {

        // Check if it is another sprite (not MousePos or RandomPos)
        if (insideLoop && node.getPosition() instanceof FromExpression) {
            pointToObject = true;
        }
    }

    @Override
    public void visit(MoveSteps node) {
        if (insideLoop && pointToObject) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
        }
    }

    private void iterateStmts(StmtList node) {
        node.getStmts().forEach(stmt -> {
            if (stmt instanceof GoToPos || stmt instanceof MoveSteps) {
                stmt.accept(this);
                pointToObject = false;
            } else if (stmt instanceof PointTowards) {
                stmt.accept(this);
            } else {
                pointToObject = false;
            }
        });
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
