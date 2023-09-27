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

import java.util.List;
import java.util.Objects;

/**
 * Represents an AST node that has children.
 */
public final class AstnnAstNode implements AstnnNode {

    private static final String EMPTY = NodeType.EMPTY_NAME.name();

    private final String label;
    private final StatementType statementType;
    private final List<AstnnNode> children;
    private final int treeDepth;

    AstnnAstNode(final String label, final List<AstnnNode> children) {
        this(label, null, children);
    }

    public AstnnAstNode(final StatementType statementType, final List<AstnnNode> children) {
        this(statementType.name(), statementType, children);
    }

    public AstnnAstNode(final StatementType statementType, final AstnnNode child) {
        this(statementType.name(), statementType, List.of(child));
    }

    AstnnAstNode(final String label, final StatementType statementType, final List<AstnnNode> children) {
        Preconditions.checkArgument(!children.isEmpty(), "An AstNode must have at least one child");

        this.label = label.isBlank() ? EMPTY : label;
        this.statementType = statementType;
        this.children = children;
        this.treeDepth = computeDepth();
    }

    private int computeDepth() {
        return 1 + children.stream().mapToInt(AstnnNode::getTreeDepth).max().orElseThrow();
    }

    @Override
    public String label() {
        return this.label;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public boolean isStatement() {
        return statementType != null;
    }

    @Override
    public StatementType getStatementType() {
        return statementType;
    }

    @Override
    public int getTreeDepth() {
        return treeDepth;
    }

    @Override
    public boolean hasBlock() {
        return children.stream().map(AstnnNode::label).anyMatch(AstnnNode.BLOCK_LABEL::equals);
    }

    @Override
    public List<AstnnNode> children() {
        return children;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AstnnNode asStatementTree() {
        final List<AstnnNode> newChildren = children.stream()
                .filter(c -> !c.isStatement() && isLegitimateNode(c))
                .map(AstnnNode::asStatementTree)
                .toList();

        return AstnnAstNodeFactory.build(label, statementType, newChildren);
    }

    /**
     * Because we are summarizing both `if` and `else` branches as `children` in
     * a superfluous node labeled "if" tends to be created when mapping with {@link AstnnNode#asStatementTree()}.
     *
     * <p>To remove these nodes, we filter with the following predicate in a post-processing step. Beware: this is just
     * a heuristic, it can also remove legitimate nodes (e.g., if your code uses the String literals "if" and "try").
     */
    private boolean isLegitimateNode(final AstnnNode node) {
        return !("if".equals(node.label()) && node.isLeaf());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof AstnnAstNode node) {
            return label.equals(node.label) && children.equals(node.children);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, children, statementType);
    }

    @Override
    public String toString() {
        return "AstNode{" + "label='" + label + '\'' + ", statementType=" + statementType + ", children=" + children
                + '}';
    }
}
