package de.uni_passau.fim.se2.litterbox.analytics.goodpractices;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsToXY;

/**
 * Gliding Motion means after a key press the sprite glides to a certain position.
 */
public class GlidingMotion extends AbstractIssueFinder {

    public static final String NAME = "gliding_motion";
    private boolean keyPressed;


    @Override
    public void visit(Script node) {
        keyPressed = false;
        if (node.getEvent() instanceof KeyPressed) {
            keyPressed = true;
            super.visit(node);
            keyPressed = false;
        }
        visitChildren(node);
    }

    @Override
    public void visit(GlideSecsToXY node) {
        if (keyPressed) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
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
