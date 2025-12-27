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
package de.uni_passau.fim.se2.litterbox.injection;

import de.uni_passau.fim.se2.litterbox.ast.ScratchNodeFactory;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.*;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;

/**
 * Base class for bug injectors that mutate Scratch programs to introduce detectable bugs.
 * <p>
 * Subclasses override {@code visit} methods to identify injection points, then call
 * one of the mutation methods to create mutated program variants.
 * <p>
 * <h2>Mutation Pattern Guide</h2>
 * <p>Choose the appropriate mutation method based on your needs:
 * <ul>
 *   <li>{@link #replaceNodeImmutably} - Creates a NEW node instance to replace an existing one.
 *       Use for immutable transformations like replacing {@code Equals(a, b)} with {@code Equals(a, newB)}.</li>
 *   <li>{@link #modifyClonedNode} - Modifies the cloned AST IN-PLACE.
 *       Use when you need to add/remove elements from collections (e.g., {@code StmtList.getStmts().remove(...)}).</li>
 *   <li>{@link #modifyMultipleClonedNodes} - Applies the same modification to multiple nodes in one mutation.
 *       Use for batch operations like removing all {@code PenUp} statements.</li>
 *   <li>{@link #wrapAsSingleResult} - Wraps a custom-cloned program as a single result.
 *       Use only with custom {@link CloneVisitor} implementations for complex multi-node transformations.</li>
 * </ul>
 * <p>
 * This class also provides:
 * <ul>
 *   <li>Shared utility methods for AST navigation and manipulation</li>
 *   <li>Context helpers for determining node position in AST</li>
 *   <li>Shared {@link CloneVisitor} instance for node cloning</li>
 * </ul>
 */
public abstract class AbstractBugInjector implements BugInjector, ScratchVisitor {

    protected static final Logger log = Logger.getLogger(AbstractBugInjector.class.getName());

    /**
     * Shared CloneVisitor instance for cloning AST nodes. Stateless and thread-safe.
     */
    protected final CloneVisitor cloner = new CloneVisitor();

    /**
     * Factory for creating AST nodes with proper metadata.
     */
    protected final ScratchNodeFactory nodeFactory = new ScratchNodeFactory();

    private List<InjectionResult> injectionResults;
    private Program originalProgram;
    private int maxMutations;

    // Debug statistics - enabled via -Dinjection.debug=true
    private static final boolean DEBUG_ENABLED = Boolean.getBoolean("injection.debug");
    private int debugCandidatesFound = 0;
    private int debugPreconditionsFailed = 0;
    private int debugMutationsAttempted = 0;

    @Override
    public List<InjectionResult> inject(Program program) {
        // Reset debug stats
        debugCandidatesFound = 0;
        debugPreconditionsFailed = 0;
        debugMutationsAttempted = 0;

        // Initialize max mutations from property
        try {
            this.maxMutations = PropertyLoader.getSystemIntProperty("injection.max_mutations_per_injector");
        } catch (IllegalArgumentException e) {
            this.maxMutations = 50; // Default fallback
        }

        // Ensure parent pointers are set before visiting
        program.accept(new ParentVisitor());
        this.originalProgram = program;
        this.injectionResults = new ArrayList<>();
        program.accept(this);

        // Log debug stats if enabled
        if (DEBUG_ENABLED) {
            logDebugStats(program);
        }

        return injectionResults;
    }

    /**
     * Log debug statistics after injection. Enable with -Dinjection.debug=true
     */
    private void logDebugStats(Program program) {
        String programName = program.getIdent().getName();
        log.info(String.format("[DEBUG] %s | program=%s | candidates=%d | preconditionsFailed=%d | mutationsAttempted=%d | mutationsProduced=%d",
                getName(),
                programName,
                debugCandidatesFound,
                debugPreconditionsFailed,
                debugMutationsAttempted,
                injectionResults.size()));
    }

    /**
     * Increment candidate count for debug tracking. Call when finding a potential injection point.
     */
    protected void debugFoundCandidate() {
        if (DEBUG_ENABLED) {
            debugCandidatesFound++;
        }
    }

    /**
     * Increment precondition failure count for debug tracking. Call when skipping due to precondition.
     */
    protected void debugPreconditionFailed() {
        if (DEBUG_ENABLED) {
            debugPreconditionsFailed++;
        }
    }

    /**
     * Increment mutation attempt count for debug tracking. Call when attempting a mutation.
     */
    protected void debugMutationAttempted() {
        if (DEBUG_ENABLED) {
            debugMutationsAttempted++;
        }
    }

    @Override
    public String getName() {
        return getIssueFinder().getName();
    }

    /**
     * Skip scripts with Never events since they contain dead code.
     * Concrete injectors should call super.visit(node) to inherit this behavior.
     */
    @Override
    public void visit(Script node) {
        if (node.getEvent() instanceof Never) {
            // Don't visit children - this is dead code
            return;
        }
        visitChildren(node);
    }

    /**
     * Modifies a cloned AST by applying a modifier to the cloned counterpart of the given node.
     * <p>
     * Use this when you need to add/remove elements from collections (e.g., StmtList)
     * or modify the cloned structure in place.
     *
     * @param originalNode The node in the original AST that triggered the injection
     * @param mutator      Logic to apply to the cloned node
     * @param <T>          The type of the AST node
     */
    @SuppressWarnings("unchecked")
    protected <T extends ASTNode> void modifyClonedNode(T originalNode, Consumer<T> mutator) {
        if (reachesLimit()) {
            return;
        }

        ScriptEntity originalScript = findContainingScript(originalNode);
        String originalScratchBlocks = safeGetScratchBlocks(originalScript);
        String spriteName = findContainingActorName(originalNode);

        MappingCloneVisitor mappingVisitor = new MappingCloneVisitor();
        ASTNode clonedRootNode = originalProgram.accept(mappingVisitor);

        if (!(clonedRootNode instanceof Program clone)) {
            log.warning("Cloning program returned " + clonedRootNode.getClass().getName());
            return;
        }

        clone.accept(new ParentVisitor());
        ASTNode clonedNode = mappingVisitor.get(originalNode);

        if (clonedNode != null && originalNode.getClass().isInstance(clonedNode)) {
            try {
                mutator.accept((T) clonedNode);
                String mutatedScratchBlocks = safeGetScratchBlocks(findContainingScript(clonedNode));

                injectionResults.add(new InjectionResult(
                        clone,
                        spriteName,
                        originalScratchBlocks,
                        mutatedScratchBlocks
                ));
            } catch (Exception e) {
                log.log(WARNING, "Failed to apply mutation for " + getName(), e);
            }
        } else {
            log.warning("Could not find suitable cloned node for " + originalNode);
        }
    }

    /**
     * Modifies multiple nodes in a cloned AST using the same mutator.
     * Creates a single mutation that applies the mutator to all cloned counterparts of the given nodes.
     * <p>
     * Use this when you need to apply the same modification to multiple nodes in one mutation
     * (e.g., removing all PenUp statements from all scripts in an actor).
     *
     * @param originalNodes The nodes in the original AST to mutate
     * @param mutator       Logic to apply to each cloned node
     * @param <T>           The type of the AST nodes
     */
    @SuppressWarnings("unchecked")
    protected <T extends ASTNode> void modifyMultipleClonedNodes(List<T> originalNodes, Consumer<T> mutator) {
        if (reachesLimit() || originalNodes.isEmpty()) {
            return;
        }

        // Use the first node as the anchor for metadata
        T anchorNode = originalNodes.getFirst();
        ScriptEntity originalScript = findContainingScript(anchorNode);
        String originalScratchBlocks = safeGetScratchBlocks(originalScript);
        String spriteName = findContainingActorName(anchorNode);

        MappingCloneVisitor mappingVisitor = new MappingCloneVisitor();
        ASTNode clonedRootNode = originalProgram.accept(mappingVisitor);

        if (!(clonedRootNode instanceof Program clone)) {
            log.warning("Cloning program returned " + clonedRootNode.getClass().getName());
            return;
        }

        clone.accept(new ParentVisitor());

        // Apply the mutator to ALL cloned nodes
        boolean anyMutated = false;
        for (T originalNode : originalNodes) {
            ASTNode clonedNode = mappingVisitor.get(originalNode);
            if (clonedNode != null && originalNode.getClass().isInstance(clonedNode)) {
                try {
                    mutator.accept((T) clonedNode);
                    anyMutated = true;
                } catch (Exception e) {
                    log.log(WARNING, "Failed to apply mutation to node for " + getName(), e);
                }
            }
        }

        if (anyMutated) {
            String mutatedScratchBlocks = safeGetScratchBlocks(findContainingScript(mappingVisitor.get(anchorNode)));
            injectionResults.add(new InjectionResult(
                    clone,
                    spriteName,
                    originalScratchBlocks,
                    mutatedScratchBlocks
            ));
        }
    }

    /**
     * Replaces a node with a new instance during cloning (immutable transformation).
     * <p>
     * Use this when you need to create a new node instance (e.g., replacing an Add
     * with a new Add that has a different operand). This maintains AST immutability.
     *
     * @param originalNode The node to be replaced
     * @param transformer  Function that returns the replacement node
     * @param <T>          The type of the AST node
     */
    protected <T extends ASTNode> void replaceNodeImmutably(T originalNode, Function<T, ASTNode> transformer) {
        if (reachesLimit()) {
            return;
        }

        ScriptEntity originalScript = findContainingScript(originalNode);
        String originalScratchBlocks = safeGetScratchBlocks(originalScript);
        String spriteName = findContainingActorName(originalNode);

        TransformingCloneVisitor visitor = new TransformingCloneVisitor();
        visitor.register(originalNode, transformer);

        try {
            ASTNode clonedRoot = originalProgram.accept(visitor);
            if (clonedRoot instanceof Program mutated) {
                mutated.accept(new ParentVisitor());

                ASTNode replacement = visitor.getReplacement(originalNode);
                String mutatedScratchBlocks = safeGetScratchBlocks(
                        replacement != null ? findContainingScript(replacement) : null);

                injectionResults.add(new InjectionResult(
                        mutated,
                        spriteName,
                        originalScratchBlocks,
                        mutatedScratchBlocks
                ));
            } else {
                log.warning("Cloning program returned " + clonedRoot.getClass().getName());
            }
        } catch (Exception e) {
            log.log(WARNING, "Failed to apply immutable mutation for " + getName(), e);
        }
    }

    private boolean reachesLimit() {
        return injectionResults.size() >= this.maxMutations;
    }

    /**
     * Finds the Script or ProcedureDefinition containing the given node.
     */
    private ScriptEntity findContainingScript(ASTNode node) {
        return AstNodeUtil.findParent(node, ScriptEntity.class);
    }

    /**
     * Finds the name of the ActorDefinition (sprite/stage) containing the given node.
     * Returns empty string if not found.
     */
    private String findContainingActorName(ASTNode node) {
        ActorDefinition actor = AstNodeUtil.findParent(node, ActorDefinition.class);
        return actor != null ? actor.getIdent().getName() : "";
    }

    /**
     * Safely converts a script entity to ScratchBlocks text.
     * Returns empty string if the script is null or if conversion fails
     * (e.g., for programs parsed from ScratchBlocks that lack proper actor context).
     */
    private String safeGetScratchBlocks(ScriptEntity script) {
        if (script == null) {
            return "";
        }
        try {
            return ScratchBlocksVisitor.of(script);
        } catch (Exception e) {
            // Can fail for programs without proper actor context (e.g., test programs from parseScratchBlocks)
            log.fine("Could not get ScratchBlocks text: " + e.getMessage());
            return "";
        }
    }

    /**
     * Custom CloneVisitor that records the mapping between original and cloned nodes.
     */
    private static class MappingCloneVisitor extends CloneVisitor {
        // Use IdentityHashMap because we want to match exact object instances from the original AST
        private final Map<ASTNode, ASTNode> mapping = new IdentityHashMap<>();

        @Override
        public <T extends ASTNode> T apply(T node) {
            T cloned = super.apply(node);
            if (node != null && cloned != null) {
                mapping.put(node, cloned);
            }
            return cloned;
        }

        public ASTNode get(ASTNode original) {
            return mapping.get(original);
        }
    }

    // ==================== Injection Result Helpers ====================

    /**
     * Wraps a custom-cloned program as a single injection result.
     * <p>
     * Use this for injectors that use custom CloneVisitor approaches
     * and don't track individual script changes. This is the escape hatch
     * for complex multi-node transformations that don't fit the standard patterns.
     *
     * @param mutatedProgram The mutated program from a custom CloneVisitor
     * @return A singleton list containing the injection result
     */
    protected List<InjectionResult> wrapAsSingleResult(Program mutatedProgram) {
        return Collections.singletonList(new InjectionResult(mutatedProgram, "", "", ""));
    }

    // ==================== Shared Utility Methods ====================

    /**
     * Safely retrieves the procedure name (proccode) for the given procedure.
     * Attempts to look up the name in the program's mapping, and falls back
     * to the identifier's name if not found.
     *
     * @param program The program containing the actor and procedure
     * @param actor   The actor containing the procedure
     * @param proc    The procedure to get the name for
     * @return The proccode, or the identifier's name as a fallback
     */
    protected String getProcedureName(Program program, ActorDefinition actor, ProcedureDefinition proc) {
        if (program.getProcedureMapping() != null) {
            Map<LocalIdentifier, ProcedureInfo> actorMap = program.getProcedureMapping()
                    .getProceduresForActor(actor.getIdent().getName());
            ProcedureInfo info = actorMap.get(proc.getIdent());
            if (info != null) {
                return info.getName();
            }
        }
        return proc.getIdent().getName();
    }

    /**
     * Registers a procedure in the program's procedure mapping.
     * This is useful for duplicated or new procedures created during injection.
     *
     * @param program   The program to update
     * @param actor     The actor the procedure belongs to
     * @param proc      The procedure definition to register
     * @param proccode  The procedure code (signature name)
     */
    protected void registerProcedure(Program program, ActorDefinition actor, ProcedureDefinition proc, String proccode) {
        if (program.getProcedureMapping() != null) {
            program.getProcedureMapping().addProcedure(
                    proc.getIdent(),
                    actor.getIdent().getName(),
                    proccode,
                    proc.getParameterDefinitionList()
            );
        }
    }

    /**
     * Navigates up the AST to find the Program root from any node.
     *
     * @param node The starting node
     * @return The Program root, or null if not found
     */
    protected Program findProgramRoot(ASTNode node) {
        return AstNodeUtil.findParent(node, Program.class);
    }

    /**
     * Replaces an old statement with a new one in its parent StmtList.
     *
     * @param oldStmt The statement to replace
     * @param newStmt The replacement statement
     */
    protected void replaceInParent(Stmt oldStmt, Stmt newStmt) {
        if (oldStmt.getParentNode() instanceof StmtList stmtList) {
            List<Stmt> stmts = stmtList.getStmts();
            int index = stmts.indexOf(oldStmt);
            if (index >= 0) {
                stmts.set(index, newStmt);
                newStmt.setParentNode(stmtList);
            }
        }
    }

    /**
     * Checks if the given statement has successor statements in its parent StmtList.
     */
    protected boolean hasSuccessorStatements(Stmt stmt) {
        if (stmt.getParentNode() instanceof StmtList stmtList) {
            List<Stmt> stmts = stmtList.getStmts();
            int index = stmts.indexOf(stmt);
            return index >= 0 && index < stmts.size() - 1;
        }
        return false;
    }

    // ==================== AST Context Helpers ====================

    /**
     * Checks if the node is within a conditional expression (the condition of an if, until, wait until).
     * This does NOT return true for nodes in the body/statements of these control structures.
     */
    protected boolean isInConditionContext(ASTNode node) {
        ASTNode current = node;
        while (current != null) {
            ASTNode parent = current.getParentNode();
            if (parent instanceof IfThenStmt ifStmt) {
                // Node must be in getBoolExpr(), not in getThenStmts()
                return isAncestorOf(ifStmt.getBoolExpr(), node);
            } else if (parent instanceof IfElseStmt ifElseStmt) {
                // Node must be in getBoolExpr(), not in getThenStmts() or getElseStmts()
                return isAncestorOf(ifElseStmt.getBoolExpr(), node);
            } else if (parent instanceof UntilStmt untilStmt) {
                // Node must be in getBoolExpr(), not in getStmtList()
                return isAncestorOf(untilStmt.getBoolExpr(), node);
            } else if (parent instanceof WaitUntil) {
                // WaitUntil only has a condition, no body
                return true;
            }
            current = parent;
        }
        return false;
    }

    /**
     * Checks if the target node is the ancestor or the same as the node.
     */
    private boolean isAncestorOf(ASTNode potentialAncestor, ASTNode node) {
        ASTNode current = node;
        while (current != null) {
            if (current == potentialAncestor) {
                return true;
            }
            current = current.getParentNode();
        }
        return false;
    }

    /**
     * Checks if the node is within a loop context.
     */
    protected boolean isInsideLoop(ASTNode node) {
        return AstNodeUtil.findParent(node.getParentNode(), LoopStmt.class) != null;
    }

    /**
     * Checks if the node is within a Script or ProcedureDefinition (not variable initialization).
     */
    protected boolean isInScriptContext(ASTNode node) {
        return AstNodeUtil.findParent(node.getParentNode(), ScriptEntity.class) != null;
    }
}
