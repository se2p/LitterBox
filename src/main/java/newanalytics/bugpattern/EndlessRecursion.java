package newanalytics.bugpattern;

import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Program;
import scratch.ast.visitor.ScratchVisitor;

import java.util.LinkedList;
import java.util.List;

public class EndlessRecursion implements IssueFinder, ScratchVisitor {
    private static final String NOTE1 = "There are no endless recursions in your project.";
    private static final String NOTE2 = "Some of the sprites can contain endless recursions.";
    public static final String NAME = "endless_recursion";
    public static final String SHORT_NAME = "endlssrcrsn";
    private boolean found = false;
    private int count = 0;
    private List<String> actorNames = new LinkedList<>();
    private ActorDefinition currentActor;

    @Override
    public IssueReport check(Program program) {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }
}
