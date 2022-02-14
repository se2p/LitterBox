package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.SingularExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.MouseX;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.MouseY;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MissingLoopMousePosition extends AbstractIssueFinder {
    public static final String NAME = "missing_loop_mouse_position";
    private static final String MOTION = "missing_loop_mouse_position_motion";
    private static final String DIRECTION = "missing_loop_mouse_position_direction";
    private boolean insideGreenFlagClone = false;
    private boolean insideLoop = false;
    private boolean inMotionStmtWithoutLoop = false;

    @Override
    public void visit(Script node) {
        if (ignoreLooseBlocks && node.getEvent() instanceof Never) {
            // Ignore unconnected blocks
            return;
        }
        if (node.getEvent() instanceof GreenFlag || node.getEvent() instanceof StartedAsClone) {
            insideGreenFlagClone = true;
        }
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
    public void visit(SetXTo node) {
        checkMotionStmt(node);
    }

    @Override
    public void visit(SetYTo node) {
        checkMotionStmt(node);
    }

    @Override
    public void visit(GoToPosXY node) {
        checkMotionStmt(node);
    }

    @Override
    public void visit(GlideSecsToXY node) {
        checkMotionStmt(node);
    }

    @Override
    public void visit(PointTowards node) {
        if (insideGreenFlagClone && !insideLoop) {
            Position pos = node.getPosition();
            if (pos instanceof MousePos) {
                Hint hint = new Hint(DIRECTION);
                addIssue(node, node.getMetadata(), hint);
            }
        }
        visitChildren(node);
    }

    @Override
    public void visit(GlideSecsTo node) {
        if (insideGreenFlagClone && !insideLoop) {
            Position pos = node.getPosition();
            if (pos instanceof MousePos) {
                Hint hint = new Hint(DIRECTION);
                addIssue(node, node.getMetadata(), hint);
            }
        }
        visitChildren(node);
    }

    @Override
    public void visit(GoToPos node) {
        if (insideGreenFlagClone && !insideLoop) {
            Position pos = node.getPosition();
            if (pos instanceof MousePos) {
                Hint hint = new Hint(DIRECTION);
                addIssue(node, node.getMetadata(), hint);
            }
        }
        visitChildren(node);
    }

    private void checkMotionStmt(SpriteMotionStmt node) {
        if (insideGreenFlagClone && !insideLoop) {
            inMotionStmtWithoutLoop = true;
        }
        visitChildren(node);
        inMotionStmtWithoutLoop = false;
    }

    @Override
    public void visit(MouseX node) {
        checkMouse(node);
    }

    @Override
    public void visit(MouseY node) {
        checkMouse(node);
    }

    private void checkMouse(SingularExpression node) {
        if (inMotionStmtWithoutLoop) {
            Hint hint = new Hint(MOTION);
            addIssue(node, node.getMetadata(), hint);
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    @Override
    public Collection<String> getHintKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(DIRECTION);
        keys.add(MOTION);
        return keys;
    }
}
