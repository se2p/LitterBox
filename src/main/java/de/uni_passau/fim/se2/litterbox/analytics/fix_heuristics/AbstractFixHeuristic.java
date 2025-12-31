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

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.util.BlockIdNodeFinder;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * Abstract base class for fix heuristics that check whether a previously reported bug
 * has been fixed in a modified version of the program.
 *
 * <p>Fix heuristics work by looking for a specific block (identified by its block ID)
 * and checking if the buggy condition no longer applies. If the condition is resolved,
 * the heuristic reports a FIX issue.
 *
 * <p>Each fix heuristic is associated with a specific bug finder. The heuristic's name
 * is derived from the related bug finder's name (following the injector pattern).
 */
public abstract class AbstractFixHeuristic extends AbstractIssueFinder {

    /** The block ID of the originally reported bug location. */
    protected final String bugLocationBlockId;

    /** The bug instances found by the related bug finder in the current program. */
    protected Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> currentBugInstances = Collections.emptySet();

    /** A semantically matched node if the original block ID is missing. */
    protected ASTNode semanticMatch;

    /**
     * Creates a new fix heuristic.
     *
     * @param bugLocationBlockId the block ID where the bug was originally reported
     */
    protected AbstractFixHeuristic(String bugLocationBlockId) {
        this.bugLocationBlockId = bugLocationBlockId;
    }

    /**
     * Returns the bug finder that this fix heuristic is associated with.
     *
     * <p>This follows the injector pattern: each fix heuristic knows which bug
     * it is designed to verify as fixed.
     *
     * @return the related bug finder instance
     */
    protected abstract IssueFinder getRelatedBugFinder();

    @Override
    public String getName() {
        return getRelatedBugFinder().getName();
    }

    @Override
    public final IssueType getIssueType() {
        return IssueType.FIX;
    }

    /**
     * Checks if the given node matches the bug location block ID or the semantic match.
     *
     * @param node the AST node to check
     * @return true if this node matches the original bug location
     */
    protected boolean matchesBugLocation(ASTNode node) {
        if (AstNodeUtil.hasBlockId(node, bugLocationBlockId)) {
            return true;
        }
        return semanticMatch != null && node == semanticMatch;
    }

    /**
     * Runs the related bug finder on the given program and stores the results.
     *
     * @param program the program to check
     */
    protected void refreshBugInstances(Program program) {
        currentBugInstances = getRelatedBugFinder().check(program);
    }

    /**
     * Checks if a bug is still reported for the given node or any of its descendants.
     *
     * @param node the node to check
     * @return true if the bug finder still reports an issue for this location
     */
    protected boolean isBugStillPresent(ASTNode node) {
        for (de.uni_passau.fim.se2.litterbox.analytics.Issue issue : currentBugInstances) {
            if (isSelfOrDescendant(issue.getCodeLocation(), node)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if node is the same as or a descendant of potentialParent.
     */
    protected boolean isSelfOrDescendant(ASTNode node, ASTNode potentialParent) {
        ASTNode curr = node;
        while (curr != null) {
            if (curr == potentialParent) {
                return true;
            }
            curr = curr.getParentNode();
        }
        return false;
    }

    @Override
    public Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> check(Program program) {
        this.program = program;
        // Try to find semantic match if ID is missing (C3)
        if (BlockIdNodeFinder.find(program, bugLocationBlockId).isEmpty()) {
            findSemanticMatch(program);
        }

        refreshBugInstances(program);
        return super.check(program);
    }

    /**
     * Attempts to find a semantic match for the missing original block.
     * Default implementation does nothing; subclasses can override for specific types.
     */
    protected void findSemanticMatch(Program program) {
        // Subclasses can implement logic to find a "stunt double" for the missing block
    }
}

