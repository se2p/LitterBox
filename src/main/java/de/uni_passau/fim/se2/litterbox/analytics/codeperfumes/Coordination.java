package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.utils.Pair;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This checks for advanced coordination in the program. Advanced coordination can be achieved by a broadcast and the
 * When I Receive block or a Wait Until.
 */
public class Coordination extends AbstractIssueFinder {
    public static final String NAME = "coordination";
    private List<Pair<String>> messagesSent = new ArrayList<>();
    private List<Pair<String>> messagesReceived = new ArrayList<>();
    private boolean addComment = false;
    private Set<String> receivedMessages = new LinkedHashSet<>();


    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        messagesSent = new ArrayList<>();
        issues = new LinkedHashSet<>();
        messagesReceived = new ArrayList<>();
        addComment = false;
        program.accept(this);
        receivedMessages = new LinkedHashSet<>();

        final LinkedHashSet<Pair<String>> syncedPairs = new LinkedHashSet<>();
        for (Pair<String> sent : messagesSent) {
            for (Pair<String> received : messagesReceived) {
                if (sent.getSnd().equalsIgnoreCase(received.getSnd())) {
                    syncedPairs.add(sent);
                    receivedMessages.add(sent.getSnd());
                    break;
                }
            }
        }

        addComment = true;
        program.accept(this);

        return issues;
    }

    @Override
    public void visit(Broadcast node) {
        if (node.getMessage().getMessage() instanceof StringLiteral) {
            final String msgName = ((StringLiteral) node.getMessage().getMessage()).getText();
            if (!addComment) {
                final String actorName = currentActor.getIdent().getName();
                messagesSent.add(new Pair<>(actorName, msgName));
            } else if (receivedMessages.contains(msgName)) {
                addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
            }
        }
    }

    @Override
    public void visit(BroadcastAndWait node) {
        if (node.getMessage().getMessage() instanceof StringLiteral) {
            final String msgName = ((StringLiteral) node.getMessage().getMessage()).getText();
            if (!addComment) {
                final String actorName = currentActor.getIdent().getName();
                messagesSent.add(new Pair<>(actorName, msgName));
            } else if (receivedMessages.contains(msgName)) {
                addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
            }
        }
    }

    @Override
    public void visit(ReceptionOfMessage node) {
        if (node.getMsg().getMessage() instanceof StringLiteral) {
            if (!addComment) {
                final String actorName = currentActor.getIdent().getName();
                final String msgName = ((StringLiteral) node.getMsg().getMessage()).getText();
                messagesReceived.add(new Pair<>(actorName, msgName));
            }
        }
    }

    @Override
    public void visit(WaitUntil node) {
        if (node.getUntil() != null && !addComment) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
        }
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