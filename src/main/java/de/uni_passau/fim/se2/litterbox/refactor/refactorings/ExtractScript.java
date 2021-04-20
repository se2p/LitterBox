package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;

public class ExtractScript implements Refactoring {

    private ActorDefinition stage;
    private final Script script;
    private final ActorDefinition sprite;
    private static final String NAME = "extract_script";

    public ExtractScript(Script script) {
        this.script = script;
        this.sprite = (ActorDefinition) script.getParentNode();
    }

    @Override
    public Program apply(Program program) {
        Program refactored = program.deepCopy();
        getStage(program);
        addScriptToStage();
        removeScriptFromSprite();
        return refactored;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return NAME + "(" + script.getUniqueName() + ")";
    }

    private void getStage(Program program) {
        ActorDefinitionList actors = program.getActorDefinitionList();
        for (ActorDefinition actor: actors.getDefinitions()) {
            if (actor.isStage()) {
                stage = actor;
                break;
            }
        }
    }

    private void addScriptToStage() {
        stage.getScripts().getScriptList().add(script);
    }

    private void removeScriptFromSprite() {
        sprite.getScripts().getScriptList().remove(script);
    }
}
