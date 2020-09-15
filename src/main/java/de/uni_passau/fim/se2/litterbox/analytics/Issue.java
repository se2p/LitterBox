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

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

/**
 * The Issue represents issues that are identified in Scratch Projects.
 */
public class Issue {

    private IssueFinder finder;
    private ActorDefinition actor;
    private ASTNode node;
    private Script script;
    private ProcedureDefinition procedure;
    private Program program;
    private Metadata metaData;

    /**
     * Creates a new issue the contains the finder that created this issue, the actor in which the issue was found and
     * the ASTNode that is most specific to this issue.
     *
     * @param finder      that created this issue
     * @param program     in which this issue was found
     * @param actor       in which this issue was found
     * @param script      in which this issue was found
     * @param currentNode that is closest to the issue origin
     * @param metaData    that contains references for comments
     */
    public Issue(IssueFinder finder, Program program, ActorDefinition actor, Script script,
                 ASTNode currentNode, Metadata metaData) {
        this.finder = finder;
        this.program = program;
        this.actor = actor;
        this.script = script;
        this.node = currentNode;
        this.metaData = metaData;
    }

    /**
     * Creates a new issue the contains the finder that created this issue, the actor in which the issue was found and
     * the ASTNode that is most specific to this issue.
     *
     * @param finder      that created this issue
     * @param program     in which this issue was found
     * @param actor       in which this issue was found
     * @param procedure   in which this issue was found
     * @param currentNode that is closest to the issue origin
     * @param metaData    that contains references for comments
     */
    public Issue(IssueFinder finder, Program program, ActorDefinition actor, ProcedureDefinition procedure,
                 ASTNode currentNode, Metadata metaData) {
        this.finder = finder;
        this.program = program;
        this.actor = actor;
        this.procedure = procedure;
        this.node = currentNode;
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

    public Program getProgram() { return program; }

    public String getActorName() {
        return actor.getIdent().getName();
    }

    /**
     * Returns the script or procedure definition that is set.
     * <p>
     * The issue contains either a script or a procedure definition.
     * If a script is set, the script is returned, if no script is present a procedure definition is returned
     *
     * @return an astNode that represents a script or procedure-definition
     */
    public ASTNode getScriptOrProcedureDefinition() {
        if (script != null) {
            return script;
        } else {
            return procedure;
        }
    }

    public String getFinderName() {
        return finder.getName();
    }

    public String getTranslatedFinderName() {
        return IssueTranslator.getInstance().getName(this.finder.getName());
    }

    public String getHint() {
        return IssueTranslator.getInstance().getHint(this.finder.getName());
    }

    public ASTNode getCodeLocation() {
        return node;
    }

    public Metadata getCodeMetadata() {
        return metaData;
    }

    public String getFinderType() {
        return finder.getIssueType().toString();
    }
}
