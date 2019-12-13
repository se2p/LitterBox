package newanalytics.bugpattern;

import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.ast.model.ASTNode;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Program;
import scratch.ast.model.expression.bool.And;
import scratch.ast.model.expression.bool.Equals;
import scratch.ast.model.expression.bool.Not;
import scratch.ast.model.expression.bool.Or;
import scratch.ast.model.statement.common.WaitUntil;
import scratch.ast.model.statement.control.IfElseStmt;
import scratch.ast.model.statement.control.IfThenStmt;
import scratch.ast.model.statement.control.UntilStmt;
import scratch.ast.visitor.ScratchVisitor;
import utils.Preconditions;

import java.util.LinkedList;
import java.util.List;

public class EqualsCondition implements IssueFinder, ScratchVisitor {
    private static final String NOTE1 = "There are equals checks in conditions in your project.";
    private static final String NOTE2 = "Some of the conditions contain equals checks.";
    public static final String NAME = "equals_condition";
    public static final String SHORT_NAME = "eqlscndtn";
    private boolean found = false;
    private int count = 0;
    private List<String> actorNames = new LinkedList<>();
    private ActorDefinition currentActor;

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        program.accept(this);
        String notes = NOTE1;
        if (count > 0) {
            notes = NOTE2;
        }
        return new IssueReport(NAME, count, actorNames, notes);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(ActorDefinition actor) {
        currentActor = actor;
        if (!actor.getChildren().isEmpty()) {
            for (ASTNode child : actor.getChildren()) {
                child.accept(this);
            }
        }

        if (found) {
            found = false;
            actorNames.add(currentActor.getIdent().getName());
        }
    }

    @Override
    public void visit(WaitUntil node) {
        if(node.getUntil() instanceof Equals){
            found =true;
            count++;
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(UntilStmt node) {
        if(node.getBoolExpr() instanceof Equals){
            found =true;
            count++;
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }
}
