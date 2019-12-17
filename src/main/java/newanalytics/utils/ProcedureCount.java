package newanalytics.utils;

import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.ast.model.ASTNode;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Program;
import scratch.ast.model.procedure.ProcedureDefinition;
import scratch.ast.visitor.ScratchVisitor;
import utils.Preconditions;

import java.util.ArrayList;

public class ProcedureCount implements IssueFinder, ScratchVisitor {
    public static final String NAME = "procedure_count";
    public static final String SHORT_NAME = "prcdrcnt";

    private int count = 0;

    private ActorDefinition currentActor;

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        program.accept(this);

        return new IssueReport(NAME, count, new ArrayList<>(), "");
    }

    @Override
    public String getName() {
        return NAME;
    }


    @Override
    public void visit(ProcedureDefinition node) {
        count++;
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }
}
