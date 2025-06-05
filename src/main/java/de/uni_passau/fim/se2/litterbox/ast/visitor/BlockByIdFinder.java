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
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Objects;
import java.util.Optional;

public final class BlockByIdFinder implements ScratchVisitor {

    private final String blockId;
    private ASTNode foundBlock = null;

    private BlockByIdFinder(final String blockId) {
        Preconditions.checkNotNull(blockId);

        this.blockId = blockId;
    }

    public static Optional<ASTNode> findBlock(final ASTNode treeRoot, final String blockId) {
        final BlockByIdFinder v = new BlockByIdFinder(blockId);
        treeRoot.accept(v);
        return Optional.ofNullable(v.foundBlock);
    }

    @Override
    public void visit(ASTNode node) {
        if (Objects.equals(AstNodeUtil.getBlockId(node), blockId)) {
            foundBlock = node;
        } else {
            ScratchVisitor.super.visit(node);
        }
    }

    @Override
    public void visit(Script node) {
        visitChildren(node);
    }
}
