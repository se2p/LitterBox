package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.BackdropSwitchTo;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.NextBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdropAndWait;
import de.uni_passau.fim.se2.litterbox.utils.Pair;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Checks if for a backdrop switch event there is a according backdrop switch
 * or a switch to next, previous or random.
 */
public class BackdropSwitchAndEvent extends AbstractIssueFinder {

    public static final String NAME = "backdrop_switch_and_event";
    private List<Pair<String>> switched = new ArrayList<>();
    private List<Pair<String>> switchReceived = new ArrayList<>();
    private boolean nextRandPrev = false;
    private boolean addComment;
    private Set<String> sentMessages;

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        issues = new LinkedHashSet<>();
        switched = new ArrayList<>();
        switchReceived = new ArrayList<>();
        nextRandPrev = false;
        addComment = false;
        sentMessages = new LinkedHashSet<>();
        program.accept(this);

        for (Pair<String> received : switchReceived) {
            for (Pair<String> sent : switched) {
                if (received.getSnd().equals(sent.getSnd())) {
                    sentMessages.add(sent.getSnd());
                    break;
                }
            }
        }
        addComment = true;
        program.accept(this);
        return issues;
    }

    @Override
    public void visit(SwitchBackdrop node) {
        if (addComment) {
            return;
        }

        final String actorName = currentActor.getIdent().getName();
        final ElementChoice msgName = node.getElementChoice();
        if (msgName instanceof Next || msgName instanceof Prev || msgName instanceof Random) {
            nextRandPrev = true;
        } else if (msgName instanceof WithExpr) {
            if (((WithExpr) msgName).getExpression() instanceof StrId) {
                switched.add(new Pair(actorName, ((StrId) ((WithExpr) msgName).getExpression()).getName()));
            } else if (((WithExpr) msgName).getExpression() instanceof StringLiteral) {
                switched.add(new Pair(actorName, ((StringLiteral) ((WithExpr) msgName).getExpression()).getText()));
            } else if (((WithExpr) msgName).getExpression() instanceof AsString) {
                AsString expr = (AsString) ((WithExpr) msgName).getExpression();
                if (expr.getOperand1() instanceof StrId) {
                    switched.add(new Pair(actorName, ((StrId) expr.getOperand1()).getName()));
                } else if (expr.getOperand1() instanceof StringLiteral) {
                    switched.add(new Pair(actorName, ((StringLiteral) expr.getOperand1()).getText()));
                }
            } else if (((WithExpr) msgName).getExpression() instanceof NumExpr) {
                nextRandPrev = true;
            }
        }
    }

    @Override
    public void visit(SwitchBackdropAndWait node) {
        if (addComment) {
            return;
        }

        final String actorName = currentActor.getIdent().getName();
        final ElementChoice msgName = node.getElementChoice();
        if (msgName instanceof Next || msgName instanceof Prev || msgName instanceof Random) {
            nextRandPrev = true;
        } else if (msgName instanceof WithExpr) {
            if (((WithExpr) msgName).getExpression() instanceof StrId) {
                switched.add(new Pair<>(actorName, ((StrId) ((WithExpr) msgName).getExpression()).getName()));
            } else if (((WithExpr) msgName).getExpression() instanceof StringLiteral) {
                switched.add(new Pair<>(actorName, ((StringLiteral) ((WithExpr) msgName).getExpression()).getText()));
            } else if (((WithExpr) msgName).getExpression() instanceof AsString) {
                AsString expr = (AsString) ((WithExpr) msgName).getExpression();
                if (expr.getOperand1() instanceof StrId) {
                    switched.add(new Pair<>(actorName, ((StrId) expr.getOperand1()).getName()));
                } else if (expr.getOperand1() instanceof StringLiteral) {
                    switched.add(new Pair<>(actorName, ((StringLiteral) expr.getOperand1()).getText()));
                }
            } else if (((WithExpr) msgName).getExpression() instanceof NumExpr) {
                nextRandPrev = true;
            }
        }
    }

    @Override
    public void visit(NextBackdrop node) {
        if (!addComment) {
            nextRandPrev = true;
        }
    }

    @Override
    public void visit(Script node) {
        if (ignoreLooseBlocks && node.getEvent() instanceof Never) {
            // Ignore unconnected blocks
            return;
        }
        currentScript = node;
        if (node.getStmtList().hasStatements() && node.getEvent() instanceof BackdropSwitchTo) {
            BackdropSwitchTo event = (BackdropSwitchTo) node.getEvent();
            final String msgName = event.getBackdrop().getName();

            if (!addComment) {
                final String actorName = currentActor.getIdent().getName();
                switchReceived.add(new Pair<>(actorName, msgName));
            } else if (sentMessages.contains(msgName) || nextRandPrev) {
                addIssue(event, event.getMetadata(), IssueSeverity.MEDIUM);
            }
        }
        visitChildren(node);
        currentScript = null;
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
