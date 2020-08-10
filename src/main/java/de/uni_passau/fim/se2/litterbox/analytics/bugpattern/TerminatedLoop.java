package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.StopOtherScriptsInSprite;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;

import java.util.LinkedHashSet;
import java.util.Set;

public class TerminatedLoop extends AbstractIssueFinder {

    private static final String NAME = "terminated_loop";
    private boolean inLoop = false;
    private boolean inIf = false;

    @Override
    public Set<Issue> check(Program program) {
        issues = new LinkedHashSet<>();
        inLoop = false;
        inIf = false;

        return super.check(program);
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(IfElseStmt node) {
        inIf = true;
        visitChildren(node);
        inIf = false;
    }

    @Override
    public void visit(IfThenStmt node) {
        inIf = true;
        visitChildren(node);
        inIf = false;
    }

    @Override
    public void visit(IfStmt node) {
        inIf = true;
        visitChildren(node);
        inIf = false;
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        inLoop = true;
        visitChildren(node);
        inLoop = false;
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        inLoop = true;
        visitChildren(node);
        inLoop = false;
    }

    @Override
    public void visit(UntilStmt node) {
        inLoop = true;
        visitChildren(node);
        inLoop = false;
    }

    @Override
    public void visit(StopAll node) {
        if (inLoop && !inIf) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(StopOtherScriptsInSprite node) {
        if (inLoop && !inIf) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(StopThisScript node) {
        if (inLoop && !inIf) {
            addIssue(node, node.getMetadata());
        }
    }
}
