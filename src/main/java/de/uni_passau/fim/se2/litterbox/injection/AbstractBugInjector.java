package de.uni_passau.fim.se2.litterbox.injection;

import de.uni_passau.fim.se2.litterbox.ast.ScratchNodeFactory;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ParentVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.TransformingCloneVisitor;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;

public abstract class AbstractBugInjector implements BugInjector, ScratchVisitor {

    protected static final Logger log = Logger.getLogger(AbstractBugInjector.class.getName());

    private List<Program> mutatedPrograms;
    private Program originalProgram;
    protected final ScratchNodeFactory nodeFactory = new ScratchNodeFactory();

    /**
     * Generates a random identifier with the given prefix.
     * Useful for breaking message/procedure name references.
     *
     * @param prefix The prefix for the generated name (e.g., "invalid_message", "invalid_call")
     * @return A randomized identifier string
     */
    protected String generateRandomIdentifier(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    public List<Program> inject(Program program) {
        this.originalProgram = program;
        this.mutatedPrograms = new ArrayList<>();
        program.accept(this);
        return mutatedPrograms;
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
     * 4. Adds the mutated program to the result list.
     *
     * @param originalNode The node in the original AST that triggered the injection.
     * @param mutator      The logic to apply to the matching CLONED node.
     * @param <T>          The type of the AST node.
     */
    @SuppressWarnings("unchecked")
    protected <T extends ASTNode> void addMutation(T originalNode, Consumer<T> mutator) {
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
                    mutatedPrograms.add(clone);
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
        TransformingCloneVisitor visitor = new TransformingCloneVisitor();
        visitor.register(originalNode, transformer);

        try {
            ASTNode clonedRoot = originalProgram.accept(visitor);
            if (clonedRoot instanceof Program mutated) {
                // Set up parent node relationships in the cloned AST
                mutated.accept(new ParentVisitor());
                mutatedPrograms.add(mutated);
            } else {
                log.warning("Cloning program returned " + clonedRoot.getClass().getName());
            }
        } catch (Exception e) {
            log.log(WARNING, "Failed to apply immutable mutation for " + getName(), e);
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
     * Checks if the AST rooted at the given node contains any instance of the specified type.
     *
     * @param root The root node to search from
     * @param type The class type to search for
     * @param <T>  The type of node to find
     * @return true if at least one instance is found
     */
    protected <T extends ASTNode> boolean containsNodeType(ASTNode root, Class<T> type) {
        if (type.isInstance(root)) {
            return true;
        }
        for (ASTNode child : root.getChildren()) {
            if (containsNodeType(child, type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given node is inside a script with a Never event (dead code).
     * Scripts with Never events are detached/orphaned and will never execute.
     * Injecting bugs in such code is useless since the bugs won't manifest.
     *
     * @param node The node to check
     * @return true if the node is inside a script with a Never event
     */
    protected boolean isInDeadCode(ASTNode node) {
        ASTNode current = node;
        while (current != null) {
            if (current instanceof Script script) {
                return script.getEvent() instanceof Never;
            }
            current = current.getParentNode();
        }
        return false;
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
}

