package newanalytics.smells;

import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Program;
import utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

public class EmptySprite implements IssueFinder {
    public static final String NAME = "empty_sprite";
    public static final String SHORT_NAME = "emptysprt";
    private static final String NOTE1 = "There are no sprites without scripts in your project.";
    private static final String NOTE2 = "Some of the sprites contain no scripts.";

    public EmptySprite() {
    }

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        List<String> found = new ArrayList<>();

        final List<ActorDefinition> definitions = program.getActorDefinitionList().getDefintions();

        for (ActorDefinition actor : definitions) {
            if (actor.getScripts().getScriptList().size() == 0) {
                found.add(actor.getIdent().getName());
            }

        }
        String notes = NOTE1;
        if (found.size() > 0) {
            notes = NOTE2;
        }

        return new IssueReport(NAME, found.size(), found, notes);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
