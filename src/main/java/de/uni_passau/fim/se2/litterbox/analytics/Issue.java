/*
 * Copyright (C) 2020 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

public class Issue {

    private IssueFinder finder;
    private ActorDefinition actor;
    private AbstractNode node;
    private Script script;
    private ProcedureDefinition procedure;
    private String helpText;
    private Metadata metaData;

    public Issue(IssueFinder finder, ActorDefinition actor, AbstractNode currentNode) {
        this.finder = finder;
        this.actor = actor;
        this.node = currentNode;
    }

    public Issue(IssueFinder finder, ActorDefinition actor, Script script,
                 AbstractNode currentNode, String helpText, Metadata metaData) {
        this.finder = finder;
        this.actor = actor;
        this.script = script;
        this.node = currentNode;
        this.helpText = helpText;
        this.metaData = metaData;
    }

    public Issue(IssueFinder finder, ActorDefinition actor, ProcedureDefinition procedure,
                 AbstractNode currentNode, String helpText, Metadata metaData) {
        this.finder = finder;
        this.actor = actor;
        this.procedure = procedure;
        this.node = currentNode;
        this.helpText = helpText;
        this.metaData = metaData;
    }

    public IssueFinder getFinder() {
        return finder;
    }

    public ActorDefinition getActor() {
        return actor;
    }

    public Script getScript() {
        return script;
    }

    public ProcedureDefinition getProcedure() {
        return procedure;
    }

    public String getActorName() {
        return actor.getUniqueName(); // TODO
    }

    public String getFinderName() {
        return finder.getName();
    }

    public String getFinderShortName() {
        return finder.getShortName();
    }

    public String getHint() {
        return helpText;
    }

    public AbstractNode getCodeLocation() {
        return node;
    }

    public Metadata getCodeMetadata() {
        return metaData;
    }

}
