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

import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.*;

public abstract class AbstractIssueFinder implements IssueFinder, ScratchVisitor {

    protected ActorDefinition currentActor;
    protected Script currentScript;
    protected ProcedureDefinition currentProcedure;
    protected Set<Issue> issues = new LinkedHashSet<>();
    protected Map<LocalIdentifier, ProcedureInfo> procMap;
    protected Program program;
    protected boolean ignoreLooseBlocks = false;

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        issues = new LinkedHashSet<>();
        program.accept(this);
        return Collections.unmodifiableSet(issues);
    }

    @Override
    public void visit(ActorDefinition actor) {
        Preconditions.checkNotNull(program);
        currentActor = actor;
        procMap = program.getProcedureMapping().getProcedures().get(currentActor.getIdent().getName());
        visitChildren(actor);
    }

    @Override
    public void visit(Script script) {
        if (ignoreLooseBlocks && script.getEvent() instanceof Never) {
            // Ignore unconnected blocks
            return;
        }
        currentScript = script;
        currentProcedure = null;
        visitChildren(script);
    }

    @Override
    public void visit(ProcedureDefinition procedure) {
        currentProcedure = procedure;
        currentScript = null;
        visitChildren(procedure);
    }

    protected void addIssue(ASTNode node, Metadata metadata) {
        addIssue(node, metadata, IssueSeverity.HIGH, new Hint(getName()));
    }

    protected void addIssue(ASTNode node, Metadata metadata, Hint hint) {
        addIssue(node, metadata, IssueSeverity.HIGH, hint);
    }

    protected void addIssue(Issue issue) {
        issues.add(issue);
    }

    protected void addIssue(ASTNode node, Metadata metadata, IssueSeverity severity, Hint hint) {
        if (currentScript != null) {
            issues.add(new Issue(this, severity, program, currentActor, currentScript, node, metadata, hint));
        } else {
            assert (currentProcedure != null);
            issues.add(new Issue(this, severity, program, currentActor, currentProcedure, node, metadata, hint));
        }
    }

    protected void addIssueForSynthesizedScript(Script theScript, ASTNode node, Metadata metadata, Hint hint) {
        issues.add(new Issue(this, IssueSeverity.HIGH, program, currentActor, theScript, node, metadata, hint));
    }

    protected void addIssueWithLooseComment() {
        issues.add(new Issue(this, IssueSeverity.HIGH, program, currentActor,
                (Script) null, // TODO: There is no script
                currentActor, // TODO: There is no node?
                null,  // TODO: There is no metadata
                new Hint(getName())));
    }

    protected void addIssueWithLooseComment(Hint hint) {
        issues.add(new Issue(this, IssueSeverity.HIGH, program, currentActor,
                (Script) null, // TODO: There is no script
                currentActor, // TODO: There is no node?
                null,  // TODO: There is no metadata
                hint));
    }

    public void setIgnoreLooseBlocks(boolean value) {
        ignoreLooseBlocks = value;
    }

    public abstract IssueType getIssueType();

    @Override
    public Collection<String> getHintKeys() {
        // Default: Only one key with the name of the finder
        return Arrays.asList(getName());
    }

    @Override
    public boolean isDuplicateOf(Issue first, Issue other) {
        if (first == other) {
            // Don't check against self
            return false;
        }
        if (first.getFinder() != other.getFinder()) {
            // Can only be a duplicate if it's the same finder
            return false;
        }

        if (first.getScriptOrProcedureDefinition() == null) {
            if (other.getScriptOrProcedureDefinition() != null) {
                // Need to refer to same script
                return false;
            }
        }

        if (other.getScriptOrProcedureDefinition() == null) {
            if (first.getScriptOrProcedureDefinition() != null) {
                // Need to refer to same script
                return false;
            }
        }

        if (first.getScriptOrProcedureDefinition() == null && other.getScriptOrProcedureDefinition() == null) {
            return false;
        }

        if (!first.getScriptOrProcedureDefinition().equals(other.getScriptOrProcedureDefinition())) {
            // Need to refer to same script
            return false;
        }

        if (first.getCodeLocation().equals(other.getCodeLocation())) {
            // Same block, so assume it's a duplicate
            return true;
        }
        return false;
    }

    @Override
    public boolean isSubsumedBy(Issue first, Issue other) {
        return false;
    }
}
