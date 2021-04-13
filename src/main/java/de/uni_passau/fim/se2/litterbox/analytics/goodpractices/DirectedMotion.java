package de.uni_passau.fim.se2.litterbox.analytics.goodpractices;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.PointInDirection;

/**
 * Directed Motion means after a key press the sprite points to a certain direction and moves a number of steps. That
 * means within a key press script there has to be a PointToDirection and a MoveSteps block in that order.
 */
public class DirectedMotion extends AbstractIssueFinder {
    public static final String NAME = "directed_motion";
    private boolean keyPressed = false;
    private boolean pointInDirection = false;

    @Override
    public void visit(KeyPressed node) {
        keyPressed = true;
        visitChildren(node);
    }


    @Override
    public void visit(PointInDirection node) {
        if (keyPressed) {
            pointInDirection = true;
            visitChildren(node);
        }
    }

    @Override
    public void visit(MoveSteps node) {
        if (keyPressed && pointInDirection) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
            keyPressed = false;
            pointInDirection = false;
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
