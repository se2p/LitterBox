package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class MessageNeverReceivedFix extends AbstractIssueFinder {
    public static final String NAME = "message_never_received_fix";
    private final String bugLocationBlockId;
    private boolean firstRun;
    private String message = null;

    public MessageNeverReceivedFix(String bugLocationBlockId) {
        this.bugLocationBlockId = bugLocationBlockId;
    }

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        issues = new LinkedHashSet<>();
        firstRun = true;
        program.accept(this);
        if (message != null) {
            program.accept(this);
        }
        return Collections.unmodifiableSet(issues);
    }

    @Override
    public void visit(Broadcast node) {
        if (firstRun && Objects.equals(AstNodeUtil.getBlockId(node), bugLocationBlockId)) {
            if (node.getMessage().getMessage() instanceof StringLiteral text) {
                message = text.getText();
            }
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(BroadcastAndWait node) {
        if (firstRun && Objects.equals(AstNodeUtil.getBlockId(node), bugLocationBlockId)) {
            if (node.getMessage().getMessage() instanceof StringLiteral text) {
                message = text.getText();
            }
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ReceptionOfMessage node) {
        if (!firstRun) {
            if (node.getMsg().getMessage() instanceof StringLiteral text) {
                if (text.getText().equals(message) && ScriptNotEmpty(node.getParentNode())) {
                    addIssue(node, node.getMetadata());
                }
            }
        }
        visitChildren(node);
    }

    private boolean ScriptNotEmpty(ASTNode parentNode) {
        assert (parentNode instanceof Script);
        return ((Script) parentNode).getStmtList().hasStatements();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.FIX;
    }
}
