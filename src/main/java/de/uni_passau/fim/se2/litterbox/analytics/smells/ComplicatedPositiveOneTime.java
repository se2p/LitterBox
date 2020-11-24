package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.TerminationStmt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This finder looks for a forever loop that contains an if loop that stops at least the script.
 */
public class ComplicatedPositiveOneTime extends AbstractIssueFinder {
    public static final String NAME = "complicated_positive_one_time";
    public static final String ALL_HINT = "complicated_positive_one_time_all";
    public static final String SCRIPT_HINT = "complicated_positive_one_time_script";
    public static final String CLONE_HINT = "complicated_positive_one_time_clone";
    private boolean insideForeverWithOneStmt;
    private boolean insideForeverAndIf;
    private boolean hasStop;
    private Hint hint;

    @Override
    public void visit(RepeatForeverStmt node) {
        if (node.getStmtList().getStmts().size() == 1) {
            insideForeverWithOneStmt = true;
        }
        visitChildren(node);
        insideForeverWithOneStmt = false;
    }

    @Override
    public void visit(IfThenStmt node) {
        if (insideForeverWithOneStmt) {
            insideForeverAndIf = true;
            hasStop = false;
        }
        visitChildren(node);
        if (insideForeverWithOneStmt && hasStop) {
            addIssue(node, node.getMetadata(), hint);
            hasStop = false;
        }
        insideForeverAndIf = false;
    }

    @Override
    public void visit(StopAll node) {
        if (insideForeverAndIf) {
            hasStop = true;
            hint = new Hint(ALL_HINT);
        }
    }

    @Override
    public void visit(StopThisScript node) {
        if (insideForeverAndIf) {
            hasStop = true;
            hint = new Hint(SCRIPT_HINT);
        }
    }

    @Override
    public void visit(DeleteClone node) {
        if (insideForeverAndIf) {
            hasStop = true;
            hint = new Hint(CLONE_HINT);
        }
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Collection<String> getHintKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(CLONE_HINT);
        keys.add(SCRIPT_HINT);
        keys.add(ALL_HINT);
        return keys;
    }
}
