/*
 * Copyright (C) 2019-2026 LitterBox contributors
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

/**
 * Builder for creating {@link Issue} instances.
 * Extends {@link FindingBuilder} with issue-specific fields.
 */
public class IssueBuilder extends FindingBuilder<Issue, IssueBuilder> {

    private IssueFinder finder;
    private IssueSeverity severity;
    private Hint hint;
    private ScriptEntity refactoring;
    private String originalBlockId;
    private String originalCodeSnippet;

    public IssueBuilder withFinder(IssueFinder finder) {
        this.finder = finder;
        return self();
    }

    public IssueBuilder withSeverity(IssueSeverity severity) {
        this.severity = severity;
        return self();
    }

    public IssueBuilder withHint(Hint hint) {
        this.hint = hint;
        return self();
    }

    public IssueBuilder withHint(String hintKey) {
        this.hint = Hint.fromKey(hintKey);
        return self();
    }

    public IssueBuilder withHintParameter(String key, String value) {
        this.hint.setParameter(key, value);
        return self();
    }

    public IssueBuilder withHintParameter(String key, HintPlaceholder value) {
        this.hint.setParameter(key, value);
        return self();
    }

    public IssueBuilder withRefactoring(ScriptEntity refactoring) {
        this.refactoring = refactoring;
        return self();
    }

    public IssueBuilder withOriginalBlockId(String blockId) {
        this.originalBlockId = blockId;
        return self();
    }

    public IssueBuilder withOriginalCodeSnippet(String codeSnippet) {
        this.originalCodeSnippet = codeSnippet;
        return self();
    }

    /**
     * Copies fields from an existing issue into this builder.
     *
     * @param issue the issue to copy from
     * @return this builder
     */
    public IssueBuilder fromIssue(Issue issue) {
        this.finder = issue.getFinder();
        this.severity = issue.getSeverity();
        this.program = issue.getProgram();
        this.actor = issue.getActor();
        this.script = issue.getScriptOrProcedureDefinition();
        this.currentNode = issue.getCodeLocation();
        this.metadata = issue.getCodeMetadata();
        this.hint = issue.getHint();
        this.refactoring = issue.getRefactoredScriptOrProcedureDefinition();
        return self();
    }

    // Override parent methods to also set metadata from node
    @Override
    public IssueBuilder withCurrentNode(ASTNode currentNode) {
        super.withCurrentNode(currentNode);
        if (currentNode != null && this.metadata == null) {
            this.metadata = currentNode.getMetadata();
        }
        return self();
    }

    // Legacy method name compatibility
    public IssueBuilder withMetadata(Metadata metadata) {
        return super.withMetadata(metadata);
    }

    // Legacy getter name
    public Metadata getMetaData() {
        return getMetadata();
    }

    private void validate() {
        if (finder == null) {
            throw new IllegalArgumentException("Finder not set.");
        }
        if (hint == null) {
            throw new IllegalArgumentException("Hint not set.");
        }
        if ((currentNode == null) != (script == null)) {
            throw new IllegalArgumentException("Either both or none of the currentNode and script must be set.");
        }
    }

    @Override
    public Issue build() {
        validate();
        return new Issue(this);
    }

    // Issue-specific getters

    public IssueFinder getFinder() {
        return finder;
    }

    public IssueSeverity getSeverity() {
        return severity;
    }

    public Hint getHint() {
        return hint;
    }

    public ScriptEntity getRefactoring() {
        return refactoring;
    }

    public String getOriginalBlockId() {
        return originalBlockId;
    }

    public String getOriginalCodeSnippet() {
        return originalCodeSnippet;
    }
}
