/*
 * Copyright (C) 2020 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.model;

import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;
import de.uni_passau.fim.se2.litterbox.utils.UnmodifiableListBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class AbstractNode implements ASTNode {

    protected final List<? extends ASTNode> children;

    protected ASTNode parent;

    public AbstractNode(ASTNode... children) {
        this(Arrays.asList(children));
    }

    public AbstractNode(List<? extends ASTNode> children) {

        Preconditions.checkAllArgsNotNull(children);
        this.children = UnmodifiableListBuilder.<ASTNode>builder()
                .addAll(children)
                .build();
    }

    public abstract void accept(ScratchVisitor visitor);

    public List<? extends ASTNode> getChildren() {
        return children;
    }

    @Override
    public ASTNode getParentNode() {
        return parent;
    }

    @Override
    public void setParentNode(ASTNode parent) {
        this.parent = parent;
    }

    public String getUniqueName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public BlockMetadata getMetadata() {
        return new NoBlockMetadata();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractNode that = (AbstractNode) o;
        return children.equals(that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(children);
    }
}
