package newanalytics.bugpattern;

import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.ast.model.ASTNode;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Program;
import scratch.ast.model.event.Clicked;
import scratch.ast.model.event.StartedAsClone;
import scratch.ast.model.statement.common.CreateCloneOf;
import scratch.ast.model.variable.StrId;
import scratch.ast.visitor.ScratchVisitor;
import utils.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MissingCloneInitialization implements IssueFinder, ScratchVisitor {

    public static final String NAME = "missing_clone_initialization";
    public static final String SHORT_NAME = "mssCloneInit";

    private List<String> whenStartsAsCloneActors = new ArrayList<>();
    private List<String> clonedActors = new ArrayList<>();
    private ActorDefinition currentActor;

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        whenStartsAsCloneActors = new ArrayList<>();
        clonedActors = new ArrayList<>();
        program.accept(this);
        final List<String> uninitializingActors
                = clonedActors.stream().filter(s -> !whenStartsAsCloneActors.contains(s)).collect(Collectors.toList());

        return new IssueReport(NAME, uninitializingActors.size(), uninitializingActors, "");
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
    }

    @Override
    public void visit(CreateCloneOf node) {
        if (node.getStringExpr() instanceof StrId) {
            final String spriteName = ((StrId) node.getStringExpr()).getName();
            if (spriteName.equals("_myself_")) {
                clonedActors.add(currentActor.getIdent().getName());
            } else {
                clonedActors.add(spriteName);
            }
        }
    }

    @Override
    public void visit(StartedAsClone node) {
        whenStartsAsCloneActors.add(currentActor.getIdent().getName());
    }

    @Override
    public void visit(Clicked node) {
        whenStartsAsCloneActors.add(currentActor.getIdent().getName());
    }
}
