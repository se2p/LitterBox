package newanalytics.utils;

import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.ast.model.ASTNode;
import scratch.ast.model.Program;
import scratch.ast.model.statement.common.WaitUntil;
import scratch.ast.model.statement.control.*;
import scratch.ast.visitor.ScratchVisitor;
import utils.Preconditions;

import java.util.LinkedList;
import java.util.List;

public class WeightedMethodCount implements IssueFinder, ScratchVisitor {
    public static final String NAME = "weighted_method_count";
    public static final String SHORT_NAME = "wghtdmthdcnt";
    private int count = 0;
    private List<String> actorNames = new LinkedList<>();

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        count = 1;
        actorNames = new LinkedList<>();

        program.accept(this);

        return new IssueReport(NAME, count, actorNames, "");
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(IfElseStmt node) {
        count++;
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(IfThenStmt node) {
        count++;
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(WaitUntil node) {
        count++;
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(UntilStmt node) {
        count++;
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        count++;
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        count++;
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }
}
