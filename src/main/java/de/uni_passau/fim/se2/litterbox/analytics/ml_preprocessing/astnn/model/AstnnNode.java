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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Represents a node in the simplified abstract syntax tree.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public sealed interface AstnnNode permits AstnnAstNode, AstnnAstLeaf {

    String BLOCK_LABEL = "BLOCK";

    /**
     * Getter needed for Jackson to discover the attribute.
     *
     * @return The label of the node.
     */
    String label();

    default String getLabel() {
        return label();
    }

    /**
     * Checks if the node has no further children.
     *
     * <p>Can be ignored for the JSON, as there the children list is just empty.
     *
     * @return Ture, if the node has no children.
     */
    @JsonIgnore
    boolean isLeaf();

    /**
     * Checks if the AST node is a statement.
     *
     * <p>Only included in the JSON if {@code true}.
     *
     * @return True, if the node is a statement.
     */
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    boolean isStatement();

    @JsonIgnore
    StatementType getStatementType();

    @JsonIgnore
    int getTreeDepth();

    /**
     * Checks if the node has a block child, i.e., if it is a block-statement.
     *
     * @return True, if the node is a block-statement.
     */
    boolean hasBlock();

    List<AstnnNode> children();

    /**
     * Getter needed for Jackson to discover the attribute.
     *
     * @return The children of the node.
     */
    default List<AstnnNode> getChildren() {
        return children();
    }

    /**
     * Turns a tree into a statement tree by removing all direct and transitive
     * children that are statements.
     *
     * @return A new AST node that does not contain statements further down in
     *         the tree.
     */
    AstnnNode asStatementTree();

}
