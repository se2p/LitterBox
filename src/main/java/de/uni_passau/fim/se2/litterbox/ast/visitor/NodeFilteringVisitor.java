/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NodeFilteringVisitor<T extends ASTNode> implements ScratchVisitor {
    private final Class<T> blockType;
    private final List<T> blocks = new ArrayList<>();

    private NodeFilteringVisitor(final Class<T> blockType) {
        this.blockType = blockType;
    }

    /**
     * Finds all blocks of the given type in the AST.
     *
     * @param root The root of the searched AST.
     * @param blockType The type of block to search for.
     * @param <T> The type of block to search for.
     * @return All blocks in the subtree of {@code root} of type {@code blockType}.
     */
    public static <T extends ASTNode> List<T> getBlocks(final ASTNode root, final Class<T> blockType) {
        final NodeFilteringVisitor<T> v = new NodeFilteringVisitor<>(blockType);
        root.accept(v);
        return v.blocks;
    }

    /**
     * Finds a block of the given type in the AST.
     *
     * @param root The root of the searched AST.
     * @param blockType The type of block to search for.
     * @param <T> The type of block to search for.
     * @return The first found block in the subtree of {@code root} of type {@code blockType}.
     */
    public static <T extends ASTNode> Optional<T> getBlock(final ASTNode root, final Class<T> blockType) {
        return getBlocks(root, blockType).stream().findFirst();
    }

    @Override
    public void visit(ASTNode node) {
        if (blockType.isAssignableFrom(node.getClass())) {
            @SuppressWarnings("unchecked")
            final T tNode = (T) node;
            blocks.add(tNode);
        }

        ScratchVisitor.super.visit(node);
    }
}
