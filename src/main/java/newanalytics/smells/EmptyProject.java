package newanalytics.smells;

import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Program;
import scratch.ast.visitor.ScratchVisitor;
import utils.Preconditions;

import java.util.ArrayList;


public class EmptyProject implements ScratchVisitor, IssueFinder {
    public static final String NAME = "empty_project";
    public static final String SHORT_NAME = "emptyprjct";
    private int count = 0;
    private boolean foundScript = false;

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        count = 0;
        foundScript = false;
        program.accept(this);
        if (!foundScript) {
            count++;
        }
        return new IssueReport(NAME, count, new ArrayList<>(), "");
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(ActorDefinition actor) {
        if (!(actor.getScripts().getScriptList().isEmpty() && actor.getProcedureDefinitionList().getList().isEmpty())) {
            foundScript = true;
        }
    }
}
