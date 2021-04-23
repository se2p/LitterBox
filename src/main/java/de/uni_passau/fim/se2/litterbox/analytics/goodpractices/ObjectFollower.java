package de.uni_passau.fim.se2.litterbox.analytics.goodpractices;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.PointTowards;

/**
 * The sprite follows another object (another sprite).
 */
public class ObjectFollower extends AbstractIssueFinder {

    public static final String NAME = "object_follower";
    private boolean insideLoop = false;


    @Override
    public void visit(RepeatForeverStmt node) {
        insideLoop = true;
        visitChildren(node);
        insideLoop = false;
    }

    @Override
    public void visit(PointTowards node) {

        // Check if it is another sprite (not MousePos or RandomPos)
        if (node.getPosition() instanceof FromExpression && insideLoop) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
        }
        visitChildren(node);

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
