package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.TerminationStmt;

import java.util.LinkedHashSet;
import java.util.Set;

public class BlockControllingLoop extends AbstractIssueFinder {

    private boolean loopFound;
    private IssueBuilder builder;
    private String answer;
    private Set<String> blocksInScript;

    @Override
    public Set<Issue> check(Program program) {
        blocksInScript = new LinkedHashSet<>();
        return super.check(program);
    }

    public void visit(Script node) {
        loopFound = false;
        builder = null;
        answer = "";
        blocksInScript.clear();
        super.visit(node);

        if (loopFound && blocksInScript.size() > 1) {
            Hint hint = new Hint(getName());
            hint.setParameter(Hint.CHOICES, blocksInScript.toString());
            hint.setParameter(Hint.ANSWER, answer);
            addIssue(builder.withHint(hint));
        }
    }

    public void visit(RepeatTimesStmt node) {
        if (!loopFound) {
            loopFound = true;
            answer = node.getTimes().getScratchBlocks();
            builder = prepareIssueBuilder(node).withSeverity(IssueSeverity.LOW);
        }
        ASTNode times = node.getTimes();
        if (times instanceof NumberLiteral) {
            blocksInScript.add("[lit]" + times.getScratchBlocks() + "[/lit]");
        }
        else {
            blocksInScript.add("[sbi]" + times.getScratchBlocks() + "[/sbi]");
        }
        visit(node.getStmtList());
    }

    public void visit(UntilStmt node) {
        if (!loopFound) {
            loopFound = true;
            answer = node.getBoolExpr().getScratchBlocks();
            builder = prepareIssueBuilder(node).withSeverity(IssueSeverity.LOW);
        }
        blocksInScript.add("[sbi]" + node.getBoolExpr().getScratchBlocks() + "[/sbi]");
        visit(node.getStmtList());
    }

    public void visit(ControlStmt node) {
        for (ASTNode child : node.getChildren()) {
            if (child instanceof BoolExpr boolExpr) {
                visit(boolExpr);
            }
            else if (child instanceof Stmt stmt) {
                visit(stmt);
            }
            else {
                visit(child);
            }
        }
    }

    public void visit(Stmt node) {
        blocksInScript.add("[sbi]" + node.getScratchBlocks() + "[/sbi]");
        super.visit(node);
    }

    public void visit(BoolExpr node) {
        blocksInScript.add("[sbi]" + node.getScratchBlocks() + "[/sbi]");
    }

    public void visit(NumExpr node) {
        if (node instanceof NumberLiteral) {
            blocksInScript.add("[lit]" + node.getScratchBlocks() + "[/lit]");
        }
        else {
            blocksInScript.add("[sbi]" + node.getScratchBlocks() + "[/sbi]");
        }
    }

    public void visit(TerminationStmt node) {
        // Don't include Termination statements as options
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.QUESTION;
    }

    @Override
    public String getName() {
        return "block_controlling_loop";
    }
}
