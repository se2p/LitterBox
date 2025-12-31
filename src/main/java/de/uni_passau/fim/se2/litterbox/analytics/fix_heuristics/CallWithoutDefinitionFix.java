/*
 * Copyright (C) 2019-2025 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.CallWithoutDefinition;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;

import java.util.HashSet;
import java.util.Set;

/**
 * Fix heuristic for the CallWithoutDefinition bug.
 *
 * <p>The bug is reported when a procedure is called but no definition exists.
 * The bug is fixed if a definition for the procedure now exists.
 */
public class CallWithoutDefinitionFix extends AbstractFixHeuristic {

    private Set<String> proceduresDef;
    private CallStmt targetCall;
    private Script foundScript;
    private ProcedureDefinition foundProcedure;

    private final IssueFinder relatedBugFinder = new CallWithoutDefinition();

    public CallWithoutDefinitionFix(String bugLocationBlockId) {
        super(bugLocationBlockId);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> check(de.uni_passau.fim.se2.litterbox.ast.model.Program program) {
        this.program = program;
        this.issues = new java.util.LinkedHashSet<>();
        if (bugLocationBlockId != null && de.uni_passau.fim.se2.litterbox.ast.util.BlockIdNodeFinder.find(program, bugLocationBlockId).isEmpty()) {
            addIssueWithLooseComment();
            return java.util.Collections.unmodifiableSet(issues);
        }
        return super.check(program);
    }


    @Override
    public void visit(ActorDefinition actor) {
        proceduresDef = new HashSet<>();
        targetCall = null;
        super.visit(actor);

        if (targetCall != null) {
            boolean definitionExists = proceduresDef.contains(targetCall.getIdent().getName());
            boolean isMalformed = program.getProcedureMapping().checkIfMalformed(
                    currentActor.getIdent().getName() + targetCall.getIdent().getName());

            if (definitionExists || isMalformed) {
                // Restore context for addIssue
                this.currentScript = foundScript;
                this.currentProcedure = foundProcedure;
                addIssue(targetCall, targetCall.getMetadata());
            }
        }
    }

    @Override
    public void visit(ProcedureDefinition node) {
        if (procMap.containsKey(node.getIdent())) {
            proceduresDef.add(procMap.get(node.getIdent()).getName());
        }
        super.visit(node);
    }

    @Override
    public void visit(CallStmt node) {
        if (matchesBugLocation(node)) {
            targetCall = node;
            foundScript = currentScript;
            foundProcedure = currentProcedure;
        }
        super.visit(node);
    }
}
