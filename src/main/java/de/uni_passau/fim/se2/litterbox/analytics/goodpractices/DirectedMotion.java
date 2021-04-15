package de.uni_passau.fim.se2.litterbox.analytics.goodpractices;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Key;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.PointInDirection;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Directed Motion means after a key press the sprite points to a certain direction and moves a number of steps. That
 * means within a key press script there has to be a PointToDirection and a MoveSteps block in that order.
 */
public class DirectedMotion extends AbstractIssueFinder {
    public static final String NAME = "directed_motion";
    private boolean pointInDirection = false;
    private boolean keyPressed;

    @Override
    public void visit(Script node) {
        keyPressed = false;
        pointInDirection = false;
        visitChildren(node);
        keyPressed = false;
        pointInDirection = false;
    }


    @Override
    public void visit(KeyPressed node) {
        keyPressed = true;
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
        return IssueType.GOOD_PRACTICE;
    }
}
