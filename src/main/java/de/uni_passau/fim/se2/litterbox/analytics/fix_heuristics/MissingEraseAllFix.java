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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingEraseAll;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenClearStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenDownStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.PenExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

import java.util.Collections;
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
    private ScriptEntity foundScriptOrProc;
    private ActorDefinition foundActor;

    private final MissingEraseAll relatedBugFinder = new MissingEraseAll();

    public MissingEraseAllFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected void resetState() {
        penClearSet = false;
        targetPenDown = null;
        foundScriptOrProc = null;
        foundActor = null;
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public Set<FixResult> check(Program program) {
        super.check(program);
        if (targetPenDown != null && penClearSet) {
            addFix(targetPenDown, targetPenDown.getMetadata(), foundActor, foundScriptOrProc);
        }
        return Collections.unmodifiableSet(fixes);
    }

    @Override
    public void visit(PenDownStmt node) {
        if (matchesBugLocation(node)) {
            targetPenDown = node;
            foundActor = currentActor;
            foundScriptOrProc = currentScript != null ? currentScript : currentProcedure;
        }
        visitChildren(node);
    }

    @Override
    public void visit(PenClearStmt node) {
        penClearSet = true;
        visitChildren(node);
    }
}
