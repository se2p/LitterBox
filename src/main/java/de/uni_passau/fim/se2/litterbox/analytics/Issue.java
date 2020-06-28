package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;

public class Issue {

    private IssueFinder finder;
    private ActorDefinition actor;
    private AbstractNode node;
    private String helpText;
    private String hintID;
    private Metadata metaData;

    public Issue(IssueFinder finder, ActorDefinition actor, AbstractNode currentNode) {
        this.finder = finder;
        this.actor = actor;
        this.node = currentNode;
    }

    public Issue(IssueFinder finder, ActorDefinition actor, AbstractNode currentNode,
                 String helpText, String hintID, Metadata metaData) {
        this.finder = finder;
        this.actor = actor;
        this.node = currentNode;
        this.helpText = helpText;
        this.hintID = hintID;
        this.metaData = metaData;
    }

    public IssueFinder getFinder() {
        return finder;
    }

    public ActorDefinition getActor() {
        return actor;
    }

    public String getActorName() {
        return actor.getUniqueName(); // TODO
    }

    public String getFinderName() {
        return finder.getName();
    }

    public String getHint() {
        return helpText;
    }

    public String getHintID() {
        return hintID;
    }

    public AbstractNode getCodeLocation() {
        return node;
    }

    public Metadata getCodeMetadata() {
        return metaData;
    }

}
