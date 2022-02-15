package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;

public class DeleteCloneInLoop extends AbstractIssueFinder {
    private static final String NAME = "delete_clone_in_loop";
    private boolean hasStartAsClone;
    private boolean insideLoop;
    private boolean insideIf;

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(Script node) {
        if (node.getEvent() instanceof StartedAsClone) {
            hasStartAsClone = true;
        }
        super.visit(node);
        hasStartAsClone = false;
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        insideLoop = true;
        super.visit(node);
        insideLoop = false;
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        insideLoop = true;
        super.visit(node);
        insideLoop = false;
    }

    @Override
    public void visit(UntilStmt node) {
        insideLoop = true;
        super.visit(node);
        insideLoop = false;
    }

    @Override
    public void visit(IfStmt node) {
        insideIf = true;
        super.visit(node);
        insideIf = false;
    }

    @Override
    public void visit(DeleteClone node) {
        if (hasStartAsClone && insideLoop && !insideIf) {
            addIssue(node, node.getMetadata());
        }
    }
}
