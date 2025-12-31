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

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ForeverInsideIf;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.util.BlockIdNodeFinder;

import java.util.Collections;
import java.util.Set;

/**
 * Fix heuristic for the ForeverInsideIf bug.
 *
 * <p>The bug is reported when a RepeatForever is inside an If with following statements.
 * The bug is fixed if:
 * <ul>
 *   <li>The forever block is no longer inside an If, OR</li>
 *   <li>No statements follow the If, OR</li>
 *   <li>The forever block was deleted (common fix)</li>
 * </ul>
 */
public class ForeverInsideIfFix extends AbstractFixHeuristic {

    private boolean insideIf;
    private int ifFollowingCounter;

    private final IssueFinder relatedBugFinder = new ForeverInsideIf();

    public ForeverInsideIfFix(String bugLocationBlockId) {
        super(bugLocationBlockId);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public Set<Issue> check(Program program) {
        insideIf = false;
        ifFollowingCounter = 0;

        // First, check if the buggy block still exists
        boolean blockExists = BlockIdNodeFinder.find(program, bugLocationBlockId).isPresent();

        if (!blockExists) {
            // Block was deleted - this is a valid fix for ForeverInsideIf
            // Report fix using a synthetic issue (the bug is gone because the block is gone)
            return createFixIssueForDeletedBlock(program);
        }

        // Block exists - run normal checking
        return super.check(program);
    }

    /**
     * Creates a FIX issue when the buggy block was deleted.
     * Uses the program as the code location since the original block no longer exists.
     */
    private Set<Issue> createFixIssueForDeletedBlock(Program program) {
        this.program = program;
        // Use addIssueWithLooseComment since we don't have a script/procedure context
        addIssueWithLooseComment();
        return Collections.unmodifiableSet(issues);
    }

    @Override
    public void visit(StmtList node) {
        int oldCounter = ifFollowingCounter;
        boolean hasIf = false;
        // Check if there's an If with following statements
        for (int i = 0; i < node.getStmts().size() - 1; i++) {
            if (node.getStmts().get(i) instanceof IfStmt) {
                ifFollowingCounter++;
                hasIf = true;
                break;
            }
        }
        visitChildren(node);
        if (hasIf) {
            ifFollowingCounter = oldCounter;
        }
    }

    @Override
    public void visit(IfStmt node) {
        boolean wasInsideIf = insideIf;
        insideIf = true;
        visitChildren(node);
        insideIf = wasInsideIf;
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        if (matchesBugLocation(node)) {
            // Fixed if not inside If OR no following statements
            if (!insideIf || ifFollowingCounter <= 0) {
                addIssue(node, node.getMetadata());
            }
        }
        visitChildren(node);
    }
}

