/*
 * Copyright (C) 2019-2024 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

public class IssueBuilder {

    private IssueFinder finder;
    private IssueSeverity severity;
    private Program program;
    private ActorDefinition actor;
    private ScriptEntity script;
    private ASTNode currentNode;
    private Metadata metaData;
    private Hint hint;
    private ScriptEntity refactoring;

    public IssueBuilder withFinder(IssueFinder finder) {
        this.finder = finder;
        return this;
    }

    public IssueBuilder withSeverity(IssueSeverity severity) {
        this.severity = severity;
        return this;
    }

    public IssueBuilder withProgram(Program program) {
        this.program = program;
        return this;
    }

    public IssueBuilder withActor(ActorDefinition actor) {
        this.actor = actor;
        return this;
    }

    public IssueBuilder withScriptOrProcedure(ScriptEntity script) {
        this.script = script;
        return this;
    }

    public IssueBuilder withScript(Script script) {
        this.script = script;
        return this;
    }

    public IssueBuilder withProcedure(ProcedureDefinition procedure) {
        this.script = procedure;
        return this;
    }

    public IssueBuilder withCurrentNode(ASTNode currentNode) {
        this.currentNode = currentNode;
        return this;
    }

    public IssueBuilder withMetadata(Metadata metaData) {
        this.metaData = metaData;
        return this;
    }

    public IssueBuilder withHint(Hint hint) {
        this.hint = hint;
        return this;
    }

    public IssueBuilder withHint(String hintKey) {
        this.hint = new Hint(hintKey);
        return this;
    }

    public IssueBuilder withHintParameter(String key, String value) {
        this.hint.setParameter(key, value);
        return this;
    }

    public IssueBuilder withRefactoring(ScriptEntity refactoring) {
        this.refactoring = refactoring;
        return this;
    }

    private void validate() {
        if (finder == null) {
            throw new IllegalArgumentException("Finder not set.");
        }
        if (hint == null) {
            throw new IllegalArgumentException("Hint not set.");
        }
        if (!finder.getHintKeys().contains(hint.getHintKey())) {
            throw new IllegalArgumentException("Hint key " + hint.getHintKey() + " is not valid.");
        }
        if ((currentNode == null) != (script == null)) {
            throw new IllegalArgumentException("Either both or none of the currentNode and script must be set.");
        }
    }

    public Issue build() {
        validate();
        return new Issue(this);
    }

    public IssueFinder getFinder() {
        return finder;
    }

    public IssueSeverity getSeverity() {
        return severity;
    }

    public Program getProgram() {
        return program;
    }

    public ActorDefinition getActor() {
        return actor;
    }

    public ScriptEntity getScript() {
        return script;
    }

    public ASTNode getCurrentNode() {
        return currentNode;
    }

    public Metadata getMetaData() {
        return metaData;
    }

    public Hint getHint() {
        return hint;
    }

    public ScriptEntity getRefactoring() {
        return refactoring;
    }
}
