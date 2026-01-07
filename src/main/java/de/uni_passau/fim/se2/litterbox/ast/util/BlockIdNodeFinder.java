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
package de.uni_passau.fim.se2.litterbox.ast.util;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.Optional;

/**
 * Utility for finding AST nodes by their block ID.
 *
 * <p>This class traverses the AST to find a node with a specific block ID and
 * captures the context (actor, script, procedure) where the node resides.
 */
public final class BlockIdNodeFinder implements ScratchVisitor {

    /**
     * Context information for a found node.
     *
     * @param node      the found AST node
     * @param actor     the containing actor
     * @param script    the containing script (null if in a procedure)
     * @param procedure the containing procedure (null if in a script)
     */
    public record NodeContext(
            ASTNode node,
            ActorDefinition actor,
            Script script,
            ProcedureDefinition procedure
    ) {
    }

    private final String targetBlockId;
    private ASTNode foundNode;
    private ActorDefinition foundActor;
    private Script foundScript;
    private ProcedureDefinition foundProcedure;

    // Traversal state
    private ActorDefinition currentActor;
    private Script currentScript;
    private ProcedureDefinition currentProcedure;

    private BlockIdNodeFinder(String targetBlockId) {
        this.targetBlockId = targetBlockId;
    }

    /**
     * Finds a node by its block ID in the given program.
     *
     * @param program the program to search
     * @param blockId the block ID to find
     * @return the node context if found, empty otherwise
     */
    public static Optional<NodeContext> find(Program program, String blockId) {
        return find((ASTNode) program, blockId);
    }

    /**
     * Finds a node by its block ID in the given AST subtree.
     *
     * @param root    the root node to start searching from
     * @param blockId the block ID to find
     * @return the node context if found, empty otherwise
     */
    public static Optional<NodeContext> find(ASTNode root, String blockId) {
        BlockIdNodeFinder finder = new BlockIdNodeFinder(blockId);
        root.accept(finder);
        if (finder.foundNode != null) {
            return Optional.of(new NodeContext(
                    finder.foundNode,
                    finder.foundActor,
                    finder.foundScript,
                    finder.foundProcedure
            ));
        }
        return Optional.empty();
    }

    /**
     * Finds a node by its block ID in the given AST subtree.
     *
     * <p>This is a convenience method that returns only the node without context.
     * Use {@link #find(ASTNode, String)} if you also need the containing actor/script/procedure.
     *
     * @param root    the root node to start searching from
     * @param blockId the block ID to find
     * @return the node if found, empty otherwise
     */
    public static Optional<ASTNode> findNode(ASTNode root, String blockId) {
        return find(root, blockId).map(NodeContext::node);
    }

    private void checkNode(ASTNode node) {
        if (foundNode != null) {
            return; // Already found
        }
        if (AstNodeUtil.hasBlockId(node, targetBlockId)) {
            foundNode = node;
            foundActor = currentActor;
            foundScript = currentScript;
            foundProcedure = currentProcedure;
        }
    }

    @Override
    public void visit(ASTNode node) {
        checkNode(node);
        if (foundNode == null) {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ActorDefinition actor) {
        checkNode(actor);
        currentActor = actor;
        visitChildren(actor);
    }

    @Override
    public void visit(Script script) {
        // Don't check the Script node itself - only track context.
        // AstNodeUtil.getBlockId(script) returns the event's block ID,
        // but we want to match the event node, not the script.
        currentScript = script;
        currentProcedure = null;
        visitChildren(script);
        currentScript = null;
    }

    @Override
    public void visit(ProcedureDefinition procedure) {
        checkNode(procedure);
        currentProcedure = procedure;
        currentScript = null;
        visitChildren(procedure);
        currentProcedure = null;
    }
}
