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
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractIssueFinder implements IssueFinder, ScratchVisitor {

    protected ActorDefinition currentActor;
    protected Script currentScript;
    protected ProcedureDefinition currentProcedure;
    protected Set<Issue> issues = new LinkedHashSet<>();
    protected Map<LocalIdentifier, ProcedureInfo> procMap;
    protected Program program;

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
        if (currentScript != null) {
            issues.add(new Issue(this, currentActor, currentScript, node, metadata));
        } else {
            assert (currentProcedure != null);
            issues.add(new Issue(this, currentActor, currentProcedure, node, metadata));
        }
    }

    protected void addIssueWithLooseComment() {
        issues.add(new Issue(this, currentActor,
                (Script) null, // TODO: There is no script
                currentActor, // TODO: There is no node?
                null)); // TODO: There is no metadata
    }

    public abstract IssueType getIssueType();
}
