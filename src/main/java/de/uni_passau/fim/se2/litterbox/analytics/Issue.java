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

import de.uni_passau.fim.se2.litterbox.analytics.clonedetection.NormalizationVisitor;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The Issue represents issues that are identified in Scratch Projects.
 */
public class Issue {

    private IssueFinder finder;
    private IssueSeverity severity;
    private ActorDefinition actor;
    private ASTNode node;
    private ASTNode normalizedNode;

    private ScriptEntity script;
    private ScriptEntity normalizedScript;
    private ScriptEntity refactoredScript;

    private Program program;
    private Metadata metaData;
    private Hint hint;
    private int id;

    private static final AtomicInteger globalIssueCount = new AtomicInteger(0);

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
    public Issue(IssueFinder finder, IssueSeverity severity, Program program, ActorDefinition actor,
                 ScriptEntity script, ASTNode currentNode, Metadata metaData, Hint hint) {
        Preconditions.checkArgument((currentNode == null) == (script == null));
        this.finder = finder;
        this.severity = severity;
        this.program = program;
        this.actor = actor;
        this.script = script;
        this.node = currentNode;
        this.normalizedNode   = normalize(this.node);
        this.normalizedScript = normalize(script);
        this.metaData = metaData;
        this.hint = hint;
        this.id = globalIssueCount.getAndIncrement();
    }

    public Issue(IssueBuilder builder) {
        this.finder = builder.getFinder();
        this.severity = builder.getSeverity();
        this.program = builder.getProgram();
        this.actor = builder.getActor();
        this.script = builder.getScript();
        this.node = builder.getCurrentNode();
        this.normalizedNode   = normalize(this.node);
        this.normalizedScript = normalize(script);
        this.refactoredScript = builder.getRefactoring();
        this.metaData = builder.getMetaData();
        this.hint = builder.getHint();
        this.id = globalIssueCount.getAndIncrement();
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

    public ActorDefinition getActor() {
        return actor;
    }

    public Script getScript() {
        return script instanceof Script s ? s : null;
    }

    public ProcedureDefinition getProcedure() {
        return script instanceof ProcedureDefinition procedure ? procedure : null;
    }

    public Program getProgram() {
        return program;
    }

    public String getActorName() {
        return actor.getIdent().getName();
    }

    /**
     * Returns the script or procedure definition that is set.
     *
     * <p>The issue contains either a script or a procedure definition.
     * If a script is set, the script is returned, if no script is present a procedure definition is returned
     *
     * @return an astNode that represents a script or procedure-definition
     */
    public ScriptEntity getScriptOrProcedureDefinition() {
        return script;
    }

    public ScriptEntity getNormalizedScriptOrProcedureDefinition() {
        return normalizedScript;
    }

    public ScriptEntity getRefactoredScriptOrProcedureDefinition() {
        return refactoredScript;
    }

    public void setRefactoredScriptOrProcedureDefinition(ScriptEntity script) {
        this.refactoredScript = script;
    }


    public String getFinderName() {
        return finder.getName();
    }

    public String getTranslatedFinderName() {
        return IssueTranslator.getInstance().getName(this.finder.getName());
    }

    public Hint getHint() {
        return hint;
    }

    public String getHintText() {
        return hint.getHintText();
    }

    public ASTNode getCodeLocation() {
        return node;
    }

    public ASTNode getNormalizedCodeLocation() {
        return normalizedNode;
    }

    public boolean isCodeLocation(ASTNode node) {
        return this.node == node;
    }

    public boolean hasMultipleBlocks() {
        return false;
    }

    public Metadata getCodeMetadata() {
        return metaData;
    }

    public int getId() {
        return id;
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

    private <T extends ASTNode> T normalize(T node) {
        if (node == null) {
            return null;
        }
        NormalizationVisitor visitor = new NormalizationVisitor();
        return (T) node.accept(visitor);
    }
}
