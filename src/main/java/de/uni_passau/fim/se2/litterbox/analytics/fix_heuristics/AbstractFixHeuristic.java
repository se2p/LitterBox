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

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.util.BlockIdNodeFinder;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

import java.util.*;

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
 *
 * <h2>Implementation Patterns</h2>
 *
 * <p>Subclasses can use one of four implementation patterns:
 *
 * <h3>Pattern A: checkWithNodeFinder utility (Simplest)</h3>
 *
 * <p>Override {@link #check(Program)} to call {@link #checkWithNodeFinder(Program)} which
 * handles block deletion detection and uses {@link #isBugStillPresent(ASTNode)} to check
 * if the bug finder still reports an issue at that location.
 * <pre>{@code
 * public Set<FixResult> check(Program program) {
 *     return checkWithNodeFinder(program);
 * }
 * }</pre>
 *
 * <h3>Pattern B: Visitor with state tracking (Most common)</h3>
 *
 * <p>Override visitor methods to track state and call {@link #addFix} when the fix is detected.
 * <pre>{@code
 * public void visit(ActorDefinition actor) {
 *     super.visit(actor);
 *     if (targetNode != null && conditionFixed) {
 *         addFix(targetNode, targetNode.getMetadata());
 *     }
 * }
 * }</pre>
 *
 * <h3>Pattern C: Custom check() with BlockIdNodeFinder</h3>
 *
 * <p>Override {@link #check(Program)} for bugs that need custom deletion detection or
 * hint-based disambiguation (multiple bugs at same block ID).
 * <pre>{@code
 * public Set<FixResult> check(Program program) {
 *     Optional<NodeContext> ctx = BlockIdNodeFinder.find(program, blockId);
 *     if (ctx.isEmpty()) {
 *         addFixWithLooseComment(); // Block deleted
 *     } else if (!isSameBugStillPresent(ctx.get())) {
 *         addFix(ctx.get().node(), ...);
 *     }
 *     return Collections.unmodifiableSet(fixes);
 * }
 * }</pre>
 *
 * <h3>Pattern D: Actor-level bugs (No block ID)</h3>
 *
 * <p>For bugs reported at the actor level (not tied to a specific block), match by
 * actor name using {@code originalIssue.sprite()} instead of block ID.
 * <pre>{@code
 * public void visit(ActorDefinition actor) {
 *     if (!actor.getIdent().getName().equals(originalIssue.sprite())) {
 *         return; // Not the target actor
 *     }
 *     super.visit(actor);
 *     if (!bugConditionStillApplies) {
 *         addFixWithLooseComment(actor);
 *     }
 * }
 * }</pre>
 *
 * <h3>Examples by Pattern</h3>
 * <ul>
 *   <li><b>Pattern A:</b> TypeErrorFix, ExpressionAsTouchingOrColorFix, RedundantAssignmentFix</li>
 *   <li><b>Pattern B:</b> ComparingLiteralsFix, EndlessRecursionFix, ForeverInsideLoopFix (most implementations)</li>
 *   <li><b>Pattern C:</b> MissingInitializationFix, VariableAsLiteralFix,
 *       ParameterAsLiteralFix (hint disambiguation)</li>
 *   <li><b>Pattern D:</b> NoWorkingScriptsFix, HideWithoutShowFix, MissingCloneCallFix (actor-level)</li>
 * </ul>
 *
 * <h3>check() Override Audit</h3>
 *
 * <p>The following 16 heuristics override {@link #check(Program)} with reason:
 *
 * <table border="1">
 *   <tr><th>Heuristic</th><th>Pattern</th><th>Reason</th></tr>
 *   <tr><td>TypeErrorFix</td><td>A</td><td>Delegates to checkWithNodeFinder</td></tr>
 *   <tr><td>ExpressionAsTouchingOrColorFix</td><td>A</td><td>Delegates to checkWithNodeFinder</td></tr>
 *   <tr><td>RedundantAssignmentFix</td><td>A</td><td>Delegates to checkWithNodeFinder</td></tr>
 *   <tr><td>MissingInitializationFix</td><td>C</td>
 *       <td>Hint-based disambiguation for multiple bugs at same block</td></tr>
 *   <tr><td>VariableAsLiteralFix</td><td>C</td><td>Custom deletion handling and scope checking</td></tr>
 *   <tr><td>ParameterAsLiteralFix</td><td>C</td><td>Custom scope checking</td></tr>
 *   <tr><td>MessageAsLiteralFix</td><td>C</td><td>Message name extraction and verification</td></tr>
 *   <tr><td>HideWithoutShowFix</td><td>D</td><td>Actor-level bug with actor name matching</td></tr>
 *   <tr><td>MissingCloneCallFix</td><td>D</td><td>Actor-level with cloning context</td></tr>
 *   <tr><td>MissingCloneInitializationFix</td><td>D</td><td>Clone context checking</td></tr>
 *   <tr><td>MessageNeverSentFix</td><td>C</td><td>Custom message sender search</td></tr>
 *   <tr><td>MessageNeverReceivedFix</td><td>C</td><td>Custom receiver search</td></tr>
 *   <tr><td>MissingAskFix</td><td>C</td><td>Answer usage verification</td></tr>
 *   <tr><td>MissingBackdropSwitchFix</td><td>C</td><td>Backdrop availability check</td></tr>
 *   <tr><td>MissingEraseAllFix</td><td>C</td><td>Pen command verification</td></tr>
 *   <tr><td>KeySetPositionFix</td><td>C</td><td>Arrow key context and type replacement detection</td></tr>
 * </table>
 */
public abstract class AbstractFixHeuristic implements FixHeuristic, ScratchVisitor {

    /**
     * The original issue that this heuristic is checking for a fix.
     */
    protected final IssueDTO originalIssue;

    /**
     * The set of fix results found during the check.
     */
    protected Set<FixResult> fixes = new LinkedHashSet<>();

    /**
     * The bug instances found by the related bug finder in the current program.
     */
    protected Set<Issue> currentBugInstances = Collections.emptySet();

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
     * Returns the name of the related bug finder.
     *
     * <p>Subclasses can override this to return a constant string instead of
     * instantiating the bug finder.
     *
     * @return the related bug finder name
     */
    protected String getRelatedBugFinderName() {
        return getRelatedBugFinder().getName();
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
    public final String getName() {
        return getRelatedBugFinderName();
    }

    /**
     * Checks if the given node matches the bug location block ID.
     *
     * @param node the AST node to check
     * @return true if this node matches the original bug location
     */
    protected boolean matchesBugLocation(ASTNode node) {
        return AstNodeUtil.hasBlockId(node, getBugLocationBlockId());
    }

    /**
     * Checks if the given node or any of its ancestors matches the bug location block ID.
     *
     * <p>This is useful for expression nodes (like {@code StringLiteral}) which typically
     * don't have their own block ID. In Scratch, block IDs are assigned to statement-level
     * blocks, not to nested expressions.
     *
     * @param node the AST node to check
     * @return true if this node or any ancestor matches the original bug location
     */
    protected boolean matchesBugLocationOrAncestor(ASTNode node) {
        ASTNode current = node;
        while (current != null) {
            if (matchesBugLocation(current)) {
                return true;
            }
            current = current.getParentNode();
        }
        return false;
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
        String originalBlockId = getBugLocationBlockId();

        for (Issue issue : currentBugInstances) {
            ASTNode bugLoc = issue.getCodeLocation();

            // If we have a block ID, try to match specifically on that
            if (originalBlockId != null && !originalBlockId.isBlank()
                    && AstNodeUtil.hasBlockId(bugLoc, originalBlockId)) {
                return true;
            }

            // Fallback: check if the bug location is a descendant of the given node
            if (isSelfOrDescendant(bugLoc, node)) {
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

    /**
     * Hook for subclasses to reset their state before each check.
     * Called at the start of {@link #check(Program)}.
     */
    protected void resetState() {
        // Default: no additional state to reset
    }

    /**
     * Initializes state and checks if the buggy block was deleted.
     *
     * <p>This is a common initialization step used by both {@link #check(Program)}
     * and {@link #checkWithNodeFinder(Program)}. It:
     * <ol>
     *   <li>Sets {@code this.program} and initializes {@code fixes}</li>
     *   <li>Calls {@link #resetState()}</li>
     *   <li>Checks if the block ID is valid</li>
     *   <li>Looks up the block in the program</li>
     *   <li>If block is deleted, adds a fix and returns empty Optional</li>
     *   <li>If block exists, returns the NodeContext</li>
     * </ol>
     *
     * @param program the program to check
     * @return Optional.empty() if block ID is null/blank or block was deleted (check fixes.isEmpty() to distinguish),
     * or Optional containing the NodeContext if block exists
     */
    protected Optional<BlockIdNodeFinder.NodeContext> initializeAndCheckBlockDeletion(Program program) {
        this.program = program;
        this.fixes = new LinkedHashSet<>();
        resetState();

        String blockId = getBugLocationBlockId();
        if (blockId == null || blockId.isBlank()) {
            // No block ID - caller should continue with visitor traversal
            return Optional.empty();
        }

        Optional<BlockIdNodeFinder.NodeContext> contextOpt = BlockIdNodeFinder.find(program, blockId);

        if (contextOpt.isEmpty()) {
            // Block deleted - this is a valid fix
            addFixWithLooseComment();
            return Optional.empty();
        }

        return contextOpt;
    }

    @Override
    public Set<FixResult> check(Program program) {
        initializeAndCheckBlockDeletion(program);

        // If a fix was already added (block was deleted), return early
        if (!fixes.isEmpty()) {
            return Collections.unmodifiableSet(fixes);
        }

        refreshBugInstances(program);

        // Visit the program to find fixes
        program.accept(this);

        return Collections.unmodifiableSet(fixes);
    }

    /**
     * Alternative check implementation that uses BlockIdNodeFinder directly.
     *
     * <p>Subclasses that don't need complex visitor-based detection can override
     * {@link #check(Program)} and call this method.
     *
     * @param program the program to check
     * @return a set of fix results
     */
    protected Set<FixResult> checkWithNodeFinder(Program program) {
        Optional<BlockIdNodeFinder.NodeContext> contextOpt = initializeAndCheckBlockDeletion(program);

        // If empty, block was deleted (fix already added) or no valid block ID
        if (contextOpt.isEmpty()) {
            return Collections.unmodifiableSet(fixes);
        }

        refreshBugInstances(program);
        BlockIdNodeFinder.NodeContext context = contextOpt.get();

        if (!isBugStillPresent(context.node())) {
            // Fixed! The block exists but the bug no longer applies.
            currentActor = context.actor();
            currentScript = context.script();
            currentProcedure = context.procedure();
            addFix(context.node(), context.node().getMetadata());
        }

        return Collections.unmodifiableSet(fixes);
    }

    /**
     * Adds a fix result for the given node.
     *
     * @param fixedNode the node where the fix was detected
     * @param metadata  the metadata associated with the node
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
     * Adds a fix result for the given node with explicit context.
     *
     * <p>Use this overload when calling addFix outside of the visitor traversal
     * (e.g., in check() after super.check() returns) where currentActor/currentScript
     * may not be set to the correct values.
     *
     * @param fixedNode         the node where the fix was detected
     * @param metadata          the metadata associated with the node
     * @param actor             the actor containing the fixed code
     * @param scriptOrProcedure the script or procedure containing the fixed code
     */
    protected void addFix(ASTNode fixedNode, Metadata metadata,
                          ActorDefinition actor, ScriptEntity scriptOrProcedure) {
        fixes.add(new FixResult(
                program,
                fixedNode,
                originalIssue,
                actor,
                scriptOrProcedure,
                metadata
        ));
    }

    /**
     * Adds a fix result with a "loose comment" - for cases where the buggy block
     * was deleted entirely or for bugs not tied to a specific block.
     *
     * @param actor the actor where the fix was detected
     */
    protected void addFixWithLooseComment(ActorDefinition actor) {
        fixes.add(new FixResult(
                program,
                null,
                originalIssue,
                actor,
                null,
                null
        ));
    }

    /**
     * Adds a fix result with a "loose comment" - for cases where the buggy block
     * was deleted entirely.
     */
    protected void addFixWithLooseComment() {
        addFixWithLooseComment(null);
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
        currentScript = null;
    }

    @Override
    public void visit(ProcedureDefinition procedure) {
        currentProcedure = procedure;
        currentScript = null;
        visitChildren(procedure);
        currentProcedure = null;
    }
}
