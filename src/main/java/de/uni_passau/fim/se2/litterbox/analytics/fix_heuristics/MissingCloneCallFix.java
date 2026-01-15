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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingCloneCall;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.clonechoice.Myself;
import de.uni_passau.fim.se2.litterbox.ast.model.clonechoice.WithCloneExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

import java.util.HashSet;
import java.util.Set;

/**
 * Fix heuristic for the MissingCloneCall bug.
 *
 * <p>The bug is reported when StartedAsClone event exists but CreateCloneOf is never called.
 * The bug is fixed if the actor is now cloned.
 */
public class MissingCloneCallFix extends AbstractFixHeuristic {

    private Set<String> clonedActors;
    private StartedAsClone targetEvent;
    private ActorDefinition foundActor;
    private Script foundScript;

    private final MissingCloneCall relatedBugFinder = new MissingCloneCall();

    public MissingCloneCallFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected void resetState() {
        clonedActors = new HashSet<>();
        targetEvent = null;
        foundActor = null;
        foundScript = null;
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public void visit(Myself node) {
        clonedActors.add(currentActor.getIdent().getName());
        visitChildren(node);
    }

    @Override
    public void visit(WithCloneExpr node) {
        if (node.getExpression() instanceof StrId strId) {
            clonedActors.add(strId.getName());
        } else if (node.getExpression() instanceof StringLiteral stringLiteral) {
            clonedActors.add(stringLiteral.getText());
        }
        visitChildren(node);
    }

    @Override
    public void visit(StartedAsClone node) {
        if (matchesBugLocation(node)) {
            targetEvent = node;
            foundActor = currentActor;
            foundScript = currentScript;
        }
        visitChildren(node);
    }

    @Override
    public Set<FixResult> check(Program program) {
        clonedActors = new HashSet<>();
        targetEvent = null;
        foundActor = null;
        foundScript = null;

        // Check if block deleted
        Set<FixResult> results = super.check(program);
        if (!results.isEmpty()) {
            return results;
        }

        if (targetEvent != null) {
            if (foundActor != null && clonedActors.contains(foundActor.getIdent().getName())) {
                this.currentActor = foundActor;
                this.currentScript = foundScript;
                addFix(targetEvent, targetEvent.getMetadata());
            }
        }

        return fixes;
    }
}
