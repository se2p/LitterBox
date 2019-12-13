package newanalytics.bugpattern;

import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.ast.model.ASTNode;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Program;
import scratch.ast.model.event.StartedAsClone;
import scratch.ast.model.statement.common.CreateCloneOf;
import scratch.ast.visitor.ScratchVisitor;
import utils.Preconditions;

import java.util.LinkedList;
import java.util.List;

public class MissingCloneCall implements IssueFinder, ScratchVisitor {
    public static final String NAME = "missing_clone_call";
    public static final String SHORT_NAME = "mssngclncll";
    private static final String NOTE1 = "There are no sprites with missing clone calls in your project.";
    private static final String NOTE2 = "Some of the sprites contain missing clone calls.";
    private int count = 0;
    private List<String> actorNames = new LinkedList<>();
    private ActorDefinition currentActor;
    private boolean foundCall = false;
    private boolean foundInit = false;

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
        foundCall = false;
        foundInit = false;
        if (!actor.getChildren().isEmpty()) {
            for (ASTNode child : actor.getChildren()) {
                child.accept(this);
            }
        }

        if (!foundCall && foundInit) {
            count++;
            actorNames.add(currentActor.getIdent().getName());
        }
    }

    @Override
    public void visit(CreateCloneOf node) {
        foundCall=true;
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(StartedAsClone node) {
        foundInit=true;
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }

    }
}
