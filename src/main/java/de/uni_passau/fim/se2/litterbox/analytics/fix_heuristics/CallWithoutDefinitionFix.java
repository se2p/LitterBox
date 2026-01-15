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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.CallWithoutDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

/**
 * Fix heuristic for the CallWithoutDefinition bug.
 *
 * <p>The bug is reported when a procedure is called but no definition exists.
 * The bug is fixed if a definition for the procedure now exists.
 */
public class CallWithoutDefinitionFix extends AbstractFixHeuristic {

    private CallStmt targetCall;
    private Script foundScript;
    private ProcedureDefinition foundProcedure;

    private final CallWithoutDefinition relatedBugFinder = new CallWithoutDefinition();

    public CallWithoutDefinitionFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected void resetState() {
        targetCall = null;
        foundScript = null;
        foundProcedure = null;
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public void visit(ActorDefinition actor) {
        targetCall = null;
        super.visit(actor);

        if (targetCall != null && targetCall.getIdent() != null && currentActor != null) {
            boolean definitionExists = false;
            if (procMap != null) {
                definitionExists = procMap.values().stream()
                        .anyMatch(info -> info.getName().equals(targetCall.getIdent().getName()));
            }

            boolean isMalformed = false;
            if (program.getProcedureMapping() != null) {
                isMalformed = program.getProcedureMapping().checkIfMalformed(
                        currentActor.getIdent().getName() + targetCall.getIdent().getName());
            }

            if (definitionExists || isMalformed) {
                // Use explicit context overload instead of mutating parent fields
                addFix(targetCall, targetCall.getMetadata(), currentActor,
                        foundScript != null ? foundScript : foundProcedure);
            }
        }
    }

    @Override
    public void visit(CallStmt node) {
        if (matchesBugLocation(node)) {
            targetCall = node;
            foundScript = currentScript;
            foundProcedure = currentProcedure;
        }
        visitChildren(node);
    }
}
