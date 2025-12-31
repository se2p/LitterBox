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
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenClearStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenDownStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.PenExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingEraseAll;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.util.BlockIdNodeFinder;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Fix heuristic for the MissingEraseAll bug.
 *
 * <p>The bug is reported when an actor uses PenDown but never EraseAll/PenClear.
 * The bug is fixed if the actor now also has an EraseAll statement.
 */
public class MissingEraseAllFix extends AbstractFixHeuristic implements PenExtensionVisitor {

    private boolean penClearSet;
    private PenDownStmt targetPenDown;
    private Script foundScript;

    private final IssueFinder relatedBugFinder = new MissingEraseAll();

    public MissingEraseAllFix(String bugLocationBlockId) {
        super(bugLocationBlockId);
    }

    @Override
    public Set<Issue> check(Program program) {
        // Check if block deleted
        if (bugLocationBlockId != null && BlockIdNodeFinder.find(program, bugLocationBlockId).isEmpty()) {
            issues = new LinkedHashSet<>();
            addIssueWithLooseComment();
            return Collections.unmodifiableSet(issues);
        }
        return super.check(program);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }


    @Override
    public void visit(ActorDefinition actor) {
        penClearSet = false;
        targetPenDown = null;
        foundScript = null;
        super.visit(actor);

        if (targetPenDown != null && penClearSet) {
            // Fixed: Erase All (PenClear) is present in the actor.
            this.currentActor = actor;
            this.currentScript = foundScript;
            addIssue(targetPenDown, targetPenDown.getMetadata());
        }
    }

    @Override
    public void visit(PenDownStmt node) {
        if (matchesBugLocation(node)) {
            targetPenDown = node;
            foundScript = currentScript;
        }
        visitChildren(node);
    }

    @Override
    public void visit(PenClearStmt node) {
        penClearSet = true;
        visitChildren(node);
    }
}
