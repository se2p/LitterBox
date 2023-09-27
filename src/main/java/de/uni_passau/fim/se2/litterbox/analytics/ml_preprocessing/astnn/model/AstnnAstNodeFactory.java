/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.astnn.model;

import com.google.common.base.Preconditions;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.AbstractToken;

import java.util.*;

import static de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.astnn.model.AstnnNode.BLOCK_LABEL;

public final class AstnnAstNodeFactory {

    private static final AstnnAstLeaf BLOCK_NODE = new AstnnAstLeaf(BLOCK_LABEL);

    private static final AstnnAstLeaf BLOCK_END_NODE = new AstnnAstLeaf(StatementType.END);

    private AstnnAstNodeFactory() {
        throw new IllegalCallerException("utility class");
    }

    /**
     * Creates a new AstnnNode with the given label and children.
     *
     * @param label    The label for the new node.
     * @param children The children of the new node.
     * @return A tree node or leaf, depending on the number of children.
     */
    public static AstnnNode build(final String label, final List<AstnnNode> children) {
        final String nodeLabel = normalizeLabel(label);
        if (children.isEmpty()) {
            return new AstnnAstLeaf(nodeLabel);
        } else {
            return new AstnnAstNode(nodeLabel, children);
        }
    }

    public static AstnnNode build(final String label) {
        return build(label, Collections.emptyList());
    }

    public static AstnnNode build(final AbstractToken token) {
        return new AstnnAstLeaf(token.name());
    }

    /**
     * Creates a new statement node.
     *
     * @param statementType The type of statement for which a node should be created.
     * @param children      The children of the new node.
     * @return A tree node or leaf, depending on the number of children.
     */
    public static AstnnNode build(final StatementType statementType, final List<AstnnNode> children) {
        if (children.isEmpty()) {
            return new AstnnAstLeaf(statementType);
        } else {
            return new AstnnAstNode(statementType, children);
        }
    }

    /**
     * Creates a new statement node.
     *
     * @param statementType The type of statement for which a node should be created.
     * @param children      The children of the new node.
     * @return A tree node or leaf, depending on the number of children.
     */
    public static AstnnNode build(final StatementType statementType, final AstnnNode... children) {
        return build(statementType, Arrays.asList(children));
    }

    /**
     * Creates a new labelled statement node.
     *
     * @param label         The label for the new node.
     * @param statementType The type of statement for which a node should be created.
     * @param children      The children of the new node.
     * @return A tree node or leaf, depending on the number of children.
     */
    public static AstnnNode build(
            final String label, final StatementType statementType, final List<AstnnNode> children
    ) {
        final String nodeLabel = normalizeLabel(label);
        if (children.isEmpty()) {
            return new AstnnAstLeaf(nodeLabel, statementType);
        } else {
            return new AstnnAstNode(nodeLabel, statementType, children);
        }
    }

    /**
     * Constructs a new regular node with the given children.
     *
     * @param nodeType The type of the node for which to construct the ASTNN node.
     * @param children The children of the node.
     * @return A new ASTNN node.
     */
    public static AstnnNode build(final NodeType nodeType, final List<AstnnNode> children) {
        if (children.isEmpty()) {
            return new AstnnAstLeaf(nodeType.toString());
        } else {
            return new AstnnAstNode(nodeType.toString(), children);
        }
    }

    /**
     * Constructs a new regular node with the given children.
     *
     * @param nodeType The class of the node for which to construct the ASTNN node.
     * @param children The children of the node.
     * @return A new ASTNN node.
     */
    public static AstnnNode build(final NodeType nodeType, final AstnnNode... children) {
        return build(nodeType, Arrays.asList(children));
    }

    public static AstnnAstLeaf block() {
        return BLOCK_NODE;
    }

    /**
     * Creates a new block node.
     *
     * @param children The children of the new node.
     * @return A block node with the given children.
     */
    public static AstnnNode block(final List<AstnnNode> children) {
        if (children.isEmpty()) {
            return block();
        } else {
            return new AstnnAstNode(BLOCK_LABEL, children);
        }
    }

    public static AstnnNode blockEnd() {
        return BLOCK_END_NODE;
    }

    /**
     * Creates a new method declaration node.
     *
     * @param name       The method name.
     * @param parameters The parameters of the method.
     * @param body       The method body.
     * @return An AST node representing the method declaration.
     */
    public static AstnnNode procedureDeclaration(
            final String name,
            final List<AstnnNode> parameters,
            final AstnnNode body
    ) {
        Preconditions.checkArgument(
                BLOCK_LABEL.equals(body.label()),
                "The body of a method must be a block."
        );

        final List<AstnnNode> children = new ArrayList<>();
        if (!parameters.isEmpty()) {
            children.add(new AstnnAstNode("parameters", parameters));
        }

        children.add(body);

        return build(name, StatementType.PROCEDURES_DEFINITION, children);
    }

    /**
     * Creates a new actor node.
     *
     * @param name       The sprite name.
     * @param procedures Custom procedures that are defined in the actor.
     * @param scripts    Scripts that are part of the actor.
     * @return An AST node representing the actor.
     */
    public static AstnnNode actorDefinition(
            final String name,
            final List<AstnnNode> procedures,
            final List<AstnnNode> scripts
    ) {
        final List<AstnnNode> children = new ArrayList<>();
        if (!procedures.isEmpty()) {
            children.add(new AstnnAstNode("procedures", procedures));
        }
        if (!scripts.isEmpty()) {
            children.add(new AstnnAstNode("scripts", scripts));
        }

        return build(name, StatementType.ACTOR, children);
    }

    public static AstnnNode program(final String name, final List<AstnnNode> actors) {
        return build(name, StatementType.PROGRAM, actors);
    }

    /**
     * Creates a new statement that contains an inner block.
     *
     * @param statementType  The class of the node for which to construct the ASTNN node.
     * @param headerChildren The children that represent the header of the block (e.g., in case of a loop).
     * @param blockChild     The inner block of the node.
     * @return An ASTNN block node.
     */
    public static AstnnAstNode blockStatement(
            final StatementType statementType, final List<AstnnNode> headerChildren, final AstnnNode blockChild
    ) {
        final List<AstnnNode> children = new ArrayList<>(headerChildren);
        children.add(blockChild);
        return new AstnnAstNode(statementType.name(), statementType, children);
    }

    /**
     * Creates a new statement that contains an inner block.
     *
     * @param statementType The type of the statement.
     * @param blockChild    The inner block.
     * @return A node of the AST.
     */
    public static AstnnAstNode blockStatement(final StatementType statementType, final AstnnNode blockChild) {
        Preconditions.checkArgument(
                BLOCK_LABEL.equals(blockChild.label()),
                "The block child of an AstNode must have the block label"
        );

        return new AstnnAstNode(statementType, blockChild);
    }

    private static String normalizeLabel(final String label) {
        return label.replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
