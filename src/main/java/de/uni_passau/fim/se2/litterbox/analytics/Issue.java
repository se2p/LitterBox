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
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

/**
 * Represents an issue (bug, smell, or perfume) identified in a Scratch project.
 * Extends {@link Finding} with issue-specific fields like severity, hints, and refactorings.
 */
public class Issue extends Finding {

    private final IssueFinder finder;
    private final IssueSeverity severity;
    private final Hint hint;
    private ScriptEntity refactoredScript;
    private final String originalBlockId;
    private final String originalCodeSnippet;

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
    protected Issue(IssueFinder finder, IssueSeverity severity, Program program, ActorDefinition actor,
                    ScriptEntity script, ASTNode currentNode, Metadata metaData, Hint hint) {
        super(program, actor, script, currentNode, metaData, null);
        Preconditions.checkArgument((currentNode == null) == (script == null));
        this.finder = finder;
        this.severity = severity;
        this.hint = hint;
        this.originalBlockId = null;
        this.originalCodeSnippet = null;
    }

    public Issue(IssueBuilder builder) {
        super(builder.getProgram(), builder.getActor(), builder.getScript(),
              builder.getCurrentNode(), builder.getMetadata(), builder.getId());
        this.finder = builder.getFinder();
        this.severity = builder.getSeverity();
        this.refactoredScript = builder.getRefactoring();
        this.hint = builder.getHint();
        this.originalBlockId = builder.getOriginalBlockId();
        this.originalCodeSnippet = builder.getOriginalCodeSnippet();
    }

    public IssueFinder getFinder() {
        return finder;
    }

    public IssueSeverity getSeverity() {
        return severity;
    }

    public IssueType getIssueType() {
        return finder.getIssueType();
    }

    @Override
    public String getFinderName() {
        return finder.getName();
    }

    public String getTranslatedFinderName(final IssueTranslator translator) {
        return translator.getName(this.finder.getName());
    }

    public Hint getHint() {
        return hint;
    }

    public String getHintText(final IssueTranslator translator) {
        return hint.getHintText(translator);
    }

    public ScriptEntity getRefactoredScriptOrProcedureDefinition() {
        return refactoredScript;
    }

    public void setRefactoredScriptOrProcedureDefinition(ScriptEntity script) {
        this.refactoredScript = script;
    }

    public boolean hasMultipleBlocks() {
        return false;
    }

    /**
     * Returns the original block ID for this issue.
     *
     * <p>This is primarily used for fixed issues where the original block may
     * no longer exist in the program. For regular issues, use
     * {@link de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil#getBlockId(ASTNode)}
     * on {@link #getCodeLocation()} instead.
     *
     * @return the original block ID, or null if not set
     */
    public String getOriginalBlockId() {
        return originalBlockId;
    }

    /**
     * Returns the original scratchblocks code snippet for this issue.
     *
     * <p>This is used for fixed issues where the original block was deleted.
     *
     * @return the original code snippet, or null if not set
     */
    public String getOriginalCodeSnippet() {
        return originalCodeSnippet;
    }

    public boolean isSubsumedBy(Issue other) {
        return finder.isSubsumedBy(this, other);
    }

    public boolean areCoupled(Issue other) {
        return finder.areCoupled(this, other);
    }

    public boolean isDuplicateOf(Issue other) {
        return finder.isDuplicateOf(this, other);
    }

    public int getDistanceTo(Issue other) {
        return finder.getDistanceTo(this, other);
    }
}
