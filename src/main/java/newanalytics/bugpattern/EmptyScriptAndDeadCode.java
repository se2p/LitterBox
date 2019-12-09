package newanalytics.bugpattern;

import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import newanalytics.smells.DeadCode;
import newanalytics.smells.EmptyScript;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Program;
import utils.Preconditions;

import java.util.List;

public class EmptyScriptAndDeadCode implements IssueFinder {
    public static final String NAME = "Simultaneous_empty_sprite_and_dead_code";
    public static final String SHORT_NAME = "simemptscrptdcode";
    private static final String NOTE1 = "There are no sprites with empty scripts and simultaneously dead code in your project.";
    private static final String NOTE2 = "Some of the sprites contain empty scripts and simultaneously dead code.";

    public EmptyScriptAndDeadCode() {
    }

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        final List<ActorDefinition> definitions = program.getActorDefinitionList().getDefintions();
        List<String> deadCode = (new DeadCode()).check(program).getPosition();
        List<String> EmptyScript = (new EmptyScript()).check(program).getPosition();
        for (ActorDefinition actor : definitions) {
            String actorName = actor.getIdent().getName();

        }

        throw new RuntimeException("not implemented");
    }

    @Override
    public String getName() {
        return NAME;
    }
}
