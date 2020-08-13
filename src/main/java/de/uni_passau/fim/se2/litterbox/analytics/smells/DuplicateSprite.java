package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.util.HashSet;
import java.util.Set;

public class DuplicateSprite extends AbstractIssueFinder {

    private static final String NAME = "duplicate_sprite";

    @Override
    public void visit(Program node) {
        Set<ActorDefinition> checked = new HashSet<>();
        ActorDefinitionList actors = node.getActorDefinitionList();
        for (ActorDefinition actor : actors.getDefinitions()) {
            for (ActorDefinition other : actors.getDefinitions()) {
                if (actor == other || checked.contains(other)) {
                    continue;
                }

                if (areActorsIdentical(actor, other)) {
                    currentActor = actor;
                    procMap = program.getProcedureMapping().getProcedures().get(currentActor.getIdent().getName());
                    addIssueWithLooseComment();
                }
            }
            checked.add(actor);
        }
    }

    private boolean areActorsIdentical(ActorDefinition actor, ActorDefinition other) {
        boolean equalScripts = actor.getScripts().equals(other.getScripts());
        boolean equalProcedures = actor.getProcedureDefinitionList().equals(other.getProcedureDefinitionList());
        boolean equalDeclarations = actor.getDecls().equals(other.getDecls());
        boolean equalActorType = actor.getActorType().equals(other.getActorType());

        return equalActorType && equalDeclarations && equalProcedures && equalScripts;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
