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
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.util.BlockIdNodeFinder;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

import java.util.Map;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Abstract base class for fix heuristics that check whether a previously reported bug
 * has been fixed in a modified version of the program.
 *
 * <p>Fix heuristics work by looking for a specific block (identified by its block ID
 * from the original issue) and checking if the buggy condition no longer applies.
 * If the condition is resolved, the heuristic reports a {@link FixResult}.
 *
 * <p>Each fix heuristic is associated with a specific bug finder. The heuristic's name
 * is derived from the related bug finder's name (following the injector pattern).
 */
public abstract class AbstractFixHeuristic implements FixHeuristic, ScratchVisitor {

    /** The original issue that this heuristic is checking for a fix. */
    protected final IssueDTO originalIssue;

    /** The set of fix results found during the check. */
    protected Set<FixResult> fixes = new LinkedHashSet<>();

    /** The bug instances found by the related bug finder in the current program. */
    protected Set<Issue> currentBugInstances = Collections.emptySet();

    /** A semantically matched node if the original block ID is missing. */
    protected ASTNode semanticMatch;

    // Context fields (similar to AbstractIssueFinder)
    protected Program program;
    protected ActorDefinition currentActor;
    protected Script currentScript;
    protected ProcedureDefinition currentProcedure;
    protected Map<LocalIdentifier, ProcedureInfo> procMap;

    /**
     * Creates a new fix heuristic.
     *
     * @param originalIssue the original issue to check for a fix
     */
    protected AbstractFixHeuristic(IssueDTO originalIssue) {
        this.originalIssue = originalIssue;
    }

    @Override
    public IssueDTO getOriginalIssue() {
        return originalIssue;
    }

    /**
     * Returns the block ID where the original bug was located.
     *
     * @return the bug location block ID
     */
    protected String getBugLocationBlockId() {
        return originalIssue.issueLocationBlockId();
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

    /**
     * Checks if the given node matches the bug location block ID or the semantic match.
     *
     * @param node the AST node to check
     * @return true if this node matches the original bug location
     */
    protected boolean matchesBugLocation(ASTNode node) {
        if (AstNodeUtil.hasBlockId(node, getBugLocationBlockId())) {
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
        for (Issue issue : currentBugInstances) {
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
    public Set<FixResult> check(Program program) {
        this.program = program;
        this.fixes = new LinkedHashSet<>();

        String blockId = getBugLocationBlockId();
        boolean blockExists = true;

        if (blockId != null && !blockId.isBlank()) {
            blockExists = !BlockIdNodeFinder.find(program, blockId).isEmpty();

            // Try to find semantic match if ID is missing (C3)
            if (!blockExists) {
                findSemanticMatch(program);
                // If the block is not found and no semantic match is found,
                // it implies the buggy block was deleted, which is valid fix.
                if (semanticMatch == null) {
                    addFixWithLooseComment();
                    return Collections.unmodifiableSet(fixes);
                }
            }
        }

        refreshBugInstances(program);

        // Visit the program to find fixes
        program.accept(this);

        return Collections.unmodifiableSet(fixes);
    }

    /**
     * Attempts to find a semantic match for the missing original block.
     * Default implementation does nothing; subclasses can override for specific types.
     */
    protected void findSemanticMatch(Program program) {
        // Subclasses can implement logic to find a "stunt double" for the missing block
    }

    /**
     * Adds a fix result for the given node.
     *
     * @param fixedNode the node where the fix was detected
     * @param metadata the metadata associated with the node
     */
    protected void addFix(ASTNode fixedNode, Metadata metadata) {
        fixes.add(new FixResult(
                program,
                fixedNode,
                originalIssue,
                currentActor,
                currentScript != null ? currentScript : currentProcedure,
                metadata
        ));
    }

    /**
     * Adds a fix result with a "loose comment" - for cases where the buggy block
     * was deleted entirely.
     */
    protected void addFixWithLooseComment() {
        fixes.add(new FixResult(
                program,
                null,
                originalIssue,
                null,
                null,
                null
        ));
    }

    // Visitor methods to track context (similar to AbstractIssueFinder)

    @Override
    public void visit(ActorDefinition actor) {
        currentActor = actor;
        procMap = program.getProcedureMapping().getProceduresForActor(actor.getIdent().getName());
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
}
