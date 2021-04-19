package de.uni_passau.fim.se2.litterbox.analytics.goodpractices;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GoToPos;

public class MouseFollower extends AbstractIssueFinder {

    public static final String NAME = "mouse_follower";
    private boolean insideLoop = false;

    @Override
    public void visit(RepeatForeverStmt node) {
        insideLoop = true;
        visitChildren(node);
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
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.GOOD_PRACTICE;
    }
}
