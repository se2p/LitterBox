package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * If an initialized variable is changed repeatedly (in a RepeatForever) after
 * a WaitSeconds block, it is seen as a timer.
 */
public class Timer extends AbstractIssueFinder {

    public static final String NAME = "timer";
    private boolean insideLoop = false;
    private boolean waitSec = false;
    private List<Qualified> setVariables = new ArrayList<>();
    private boolean addComment = false;


    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        issues = new LinkedHashSet<>();
        setVariables = new ArrayList<>();
        addComment = false;
        program.accept(this);
        addComment = true;
        program.accept(this);
        return issues;
    }

    @Override
    public void visit(SetVariableTo node) {
        if (!addComment) {
            if (node.getIdentifier() instanceof Qualified) {
                setVariables.add((Qualified) node.getIdentifier());
            }
        }
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        if (!setVariables.isEmpty() && addComment) {
            insideLoop = true;
            visitChildren(node);
            insideLoop = false;
        }
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        if (!setVariables.isEmpty() && addComment) {
            insideLoop = true;
            visitChildren(node);
            insideLoop = false;
        }
    }

    @Override
    public void visit(UntilStmt node) {
        if (!setVariables.isEmpty() && addComment) {
            insideLoop = true;
            visitChildren(node);
            insideLoop = false;
        }
    }

    @Override
    public void visit(StmtList node) {
        if (addComment && insideLoop && !setVariables.isEmpty()) {
            node.getStmts().forEach(stmt -> {
                if (stmt instanceof WaitSeconds) {
                    waitSec = true;
                }
            });
            node.getStmts().forEach(stmt -> {
                        if (stmt instanceof ChangeVariableBy) {
                            if (waitSec) {
                                if (((ChangeVariableBy) stmt).getIdentifier() instanceof Qualified) {
                                    Qualified changedVariable = (Qualified) ((ChangeVariableBy) stmt).getIdentifier();
                                    for (Qualified var : setVariables) {
                                        if (changedVariable.equals(var)) {
                                            addIssue(stmt, stmt.getMetadata(), IssueSeverity.HIGH);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                );
            waitSec = false;
        } else {
            visitChildren(node);
        }
    }

    @Override
    public boolean isDuplicateOf(Issue first, Issue other) {
        if (first == other) {
            return false;
        }
        if (first.getFinder() != other.getFinder()) {
            return false;
        }
        return true;
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
