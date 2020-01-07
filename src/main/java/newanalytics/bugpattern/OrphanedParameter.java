package newanalytics.bugpattern;

import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.ast.Constants;
import scratch.ast.model.ASTNode;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Program;
import scratch.ast.model.procedure.Parameter;
import scratch.ast.model.procedure.ProcedureDefinition;
import scratch.ast.model.variable.StrId;
import scratch.ast.visitor.ScratchVisitor;
import utils.Preconditions;

import java.util.LinkedList;
import java.util.List;

public class OrphanedParameter implements IssueFinder, ScratchVisitor {
    private static final String NOTE1 = "There are no orphaned parameters in your project.";
    private static final String NOTE2 = "Some of the procedures contain orphaned parameters.";
    public static final String NAME = "orphaned_parameter";
    public static final String SHORT_NAME = "OrphParam";
    private boolean found = false;
    private int count = 0;
    private List<String> actorNames = new LinkedList<>();
    private ActorDefinition currentActor;
    private List<Parameter> currentParameters;
    private boolean insideProcedure;

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        found = false;
        count = 0;
        actorNames = new LinkedList<>();
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
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        currentParameters = node.getParameterList().getParameterListPlain().getParameters();
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
        insideProcedure = false;
    }

    @Override
    public void visit(StrId node) {
        if (insideProcedure) {
            if (node.getName().startsWith(Constants.PARAMETER_ABBREVIATION)) {
                checkParameterNames(node.getName());
            }
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }

    }

    private void checkParameterNames(String name) {
        boolean validParametername = false;
        for (int i = 0; i < currentParameters.size() && !validParametername; i++) {
            if (name.equals(currentParameters.get(i).getIdent().getName())) {
                validParametername = true;
            }
        }
        if (!validParametername) {
            count++;
            found = true;
        }
    }
}
