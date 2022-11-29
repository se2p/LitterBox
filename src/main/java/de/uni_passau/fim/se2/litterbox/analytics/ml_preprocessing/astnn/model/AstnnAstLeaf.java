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

import java.util.List;

/**
 * Represents an AST node that has no further children.
 */
public record AstnnAstLeaf(String label, StatementType statementType) implements AstnnNode {

    private static final String EMPTY = NodeType.EMPTY_STRING.name();

    AstnnAstLeaf(final String label) {
        this(label.isEmpty() ? EMPTY : label, null);
    }

    public AstnnAstLeaf(final StatementType statementType) {
        this(statementType.name(), statementType);
    }

    @Override
    public boolean isLeaf() {
        return true;
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
        return 1;
    }

    @Override
    public boolean hasBlock() {
        return false;
    }

    @Override
    public List<AstnnNode> children() {
        return List.of();
    }

    @Override
    public AstnnNode asStatementTree() {
        return this;
    }
}
