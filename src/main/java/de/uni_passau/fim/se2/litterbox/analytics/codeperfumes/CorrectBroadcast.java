package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.utils.Pair;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This pattern is the solution to the bugs "Message Never Received" and "Message Never Sent". If both a send block
 * and the matching receive block exist, it is a correctly used broadcast.
 */
public class CorrectBroadcast extends AbstractIssueFinder {

    public static final String NAME = "correct_broadcast";
    private List<Pair<String>> messageSent = new ArrayList<>();
    private List<Pair<String>> messageReceived = new ArrayList<>();
    private boolean addComment = false;
    private Set<String> receivedMessages = new LinkedHashSet<>();

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        messageSent = new ArrayList<>();
        messageReceived = new ArrayList<>();
        issues = new LinkedHashSet<>();
        addComment = false;
        program.accept(this);
        receivedMessages = new LinkedHashSet<>();

        final LinkedHashSet<Pair<String>> syncedPairs = new LinkedHashSet<>();
        for (Pair<String> sent : messageSent) {
            for (Pair<String> received : messageReceived) {
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
                messageSent.add(new Pair<>(actorName, msgName));
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
                messageSent.add(new Pair<>(actorName, msgName));
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
                messageReceived.add(new Pair<>(actorName, msgName));
            }
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
