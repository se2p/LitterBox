package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;

public class Issue {

    private IssueFinder finder;

    public Issue(IssueFinder finder, ActorDefinition actor, AbstractNode currentNode) {
        this.finder = finder;
    }

    public String getFinderName() {
        return finder.getName();
    }

}
