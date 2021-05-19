package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.PointInDirection;

/**
 * Directed Motion means after a key press the sprite points to a certain direction and moves a number of steps. That
 * means within a key press script there has to be a PointToDirection and a MoveSteps block in that order.
 */
public class DirectedMotion extends AbstractIssueFinder {

    public static final String NAME = "directed_motion";
    private boolean pointInDirection = false;
    private boolean keyPressed = false;

    @Override
    public void visit(Script node) {
        keyPressed = false;
        pointInDirection = false;
        if (node.getEvent() instanceof KeyPressed) {
            keyPressed = true;
            super.visit(node);
            keyPressed = false;
            pointInDirection = false;
        }
        visitChildren(node);
    }

    @Override
    public void visit(StmtList node) {
        if (keyPressed) {
            for (Stmt stmt : node.getStmts()) {
                if (stmt instanceof PointInDirection) {
                    pointInDirection = true;
                } else if (stmt instanceof MoveSteps) {
                    if (pointInDirection) {
                        addIssue(stmt, stmt.getMetadata(), IssueSeverity.MEDIUM);
                        break;
                    }
                }
            }
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
