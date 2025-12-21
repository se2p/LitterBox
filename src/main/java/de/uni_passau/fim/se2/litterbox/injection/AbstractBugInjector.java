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
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ParentVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.TransformingCloneVisitor;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;

public abstract class AbstractBugInjector implements BugInjector, ScratchVisitor {

    protected static final Logger log = Logger.getLogger(AbstractBugInjector.class.getName());

    private List<InjectionResult> injectionResults;
    private Program originalProgram;
    protected final ScratchNodeFactory nodeFactory = new ScratchNodeFactory();

    @Override
    public List<InjectionResult> inject(Program program) {
        this.originalProgram = program;
        this.injectionResults = new ArrayList<>();
        program.accept(this);
        return injectionResults;
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
     * Adds a mutation for the given original node.
     * This method:
     * 1. Clones the original program using a MappingCloneVisitor to track node correspondence.
     * 2. Retrieves the cloned counterpart of the originalNode.
     * 3. Applies the mutation logic to the cloned node.
     * 4. Adds the mutated program to the result list with ScratchBlocks metadata.
     *
     * @param originalNode The node in the original AST that triggered the injection.
     * @param mutator      The logic to apply to the matching CLONED node.
     * @param <T>          The type of the AST node.
     */
    @SuppressWarnings("unchecked")
    protected <T extends ASTNode> void addMutation(T originalNode, Consumer<T> mutator) {
        // Capture original script and sprite BEFORE cloning
        String originalScratchBlocks = safeGetScratchBlocks(findContainingScript(originalNode));
        String spriteName = findContainingActorName(originalNode);

        MappingCloneVisitor mappingVisitor = new MappingCloneVisitor();

        // Clone the program and capture mapping
        // A standard CloneVisitor would not expose this mapping.
        ASTNode clonedRootNode = originalProgram.accept(mappingVisitor);
        if (!(clonedRootNode instanceof Program clone)) {
            log.warning("Cloning program returned " + clonedRootNode.getClass().getName());
            return;
        }

        // Find the cloned node corresponding to the original node
        ASTNode clonedNode = mappingVisitor.get(originalNode);

        if (clonedNode != null) {
            // Verify class compatibility
            if (originalNode.getClass().isInstance(clonedNode)) {
                try {
                    mutator.accept((T) clonedNode);

                    // Capture mutated script ScratchBlocks AFTER mutation
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
                log.warning("Cloned node type mismatch. Expected " + originalNode.getClass().getName() + " but got " + clonedNode.getClass().getName());
            }
        } else {
            // This can happen if the node was not visited/cloned (e.g. if it's metadata the visitor skipped, though unlikely for Stmts)
            log.warning("Could not find cloned node for original: " + originalNode);
        }
    }

    /**
     * Adds an immutable mutation for the given original node.
     * <p>
     * Unlike {@link #addMutation}, this method maintains AST immutability by creating
     * new node instances during cloning rather than modifying cloned nodes via setters.
     * <p>
     * Usage:
     * <pre>
     * addImmutableMutation(originalMoveSteps, node ->
     *     new MoveSteps(newStepsExpr, node.getMetadata()));
     * </pre>
     *
     * @param originalNode The node in the original AST that should be transformed.
     * @param transformer  Function that takes the original node and returns a new replacement node.
     * @param <T>          The type of the AST node.
     */
    protected <T extends ASTNode> void addImmutableMutation(T originalNode, Function<T, ASTNode> transformer) {
        // Capture original script and sprite BEFORE transformation
        String originalScratchBlocks = safeGetScratchBlocks(findContainingScript(originalNode));
        String spriteName = findContainingActorName(originalNode);

        TransformingCloneVisitor visitor = new TransformingCloneVisitor();
        visitor.register(originalNode, transformer);

        try {
            ASTNode clonedRoot = originalProgram.accept(visitor);
            if (clonedRoot instanceof Program mutated) {
                // Set up parent node relationships in the cloned AST
                mutated.accept(new ParentVisitor());

                // Get mutated script ScratchBlocks from the replacement node
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

    /**
     * Finds the Script or ProcedureDefinition containing the given node.
     */
    private ScriptEntity findContainingScript(ASTNode node) {
        ASTNode current = node;
        while (current != null) {
            if (current instanceof Script script) {
                return script;
            }
            if (current instanceof ProcedureDefinition proc) {
                return proc;
            }
            current = current.getParentNode();
        }
        return null;
    }

    /**
     * Finds the name of the ActorDefinition (sprite/stage) containing the given node.
     * Returns empty string if not found.
     */
    private String findContainingActorName(ASTNode node) {
        ASTNode current = node;
        while (current != null) {
            if (current instanceof ActorDefinition actor) {
                return actor.getIdent().getName();
            }
            current = current.getParentNode();
        }
        return "";
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

    // ==================== Shared Utility Methods ====================

    /**
     * Navigates up the AST to find the Program root from any node.
     *
     * @param node The starting node
     * @return The Program root, or null if not found
     */
    protected Program findProgramRoot(ASTNode node) {
        ASTNode current = node;
        while (current != null) {
            if (current instanceof Program program) {
                return program;
            }
            current = current.getParentNode();
        }
        return null;
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
        ASTNode parent = stmt.getParentNode();
        if (parent instanceof StmtList stmtList) {
            List<Stmt> stmts = stmtList.getStmts();
            int index = stmts.indexOf(stmt);
            return index >= 0 && index < stmts.size() - 1;
        }
        return false;
    }

    // ==================== AST Context Helpers ====================

    /**
     * Checks if the node is within a conditional context (if, until, wait until).
     */
    protected boolean isInConditionContext(ASTNode node) {
        ASTNode parent = node.getParentNode();
        while (parent != null) {
            if (parent instanceof IfThenStmt || parent instanceof IfElseStmt
                    || parent instanceof UntilStmt || parent instanceof WaitUntil) {
                return true;
            }
            parent = parent.getParentNode();
        }
        return false;
    }

    /**
     * Checks if the node is within a loop context.
     */
    protected boolean isInsideLoop(ASTNode node) {
        ASTNode parent = node.getParentNode();
        while (parent != null) {
            if (parent instanceof LoopStmt) {
                return true;
            }
            parent = parent.getParentNode();
        }
        return false;
    }

    /**
     * Checks if the node is within a Script or ProcedureDefinition (not variable initialization).
     */
    protected boolean isInScriptContext(ASTNode node) {
        ASTNode parent = node.getParentNode();
        while (parent != null) {
            if (parent instanceof Script || parent instanceof ProcedureDefinition) {
                return true;
            }
            parent = parent.getParentNode();
        }
        return false;
    }
}
