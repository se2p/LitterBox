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
package de.uni_passau.fim.se2.litterbox.ast.model.expression;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public abstract class UnaryExpression<A extends ASTNode> extends AbstractNode {

    private final A operand1;
    private final BlockMetadata metadata;

    protected UnaryExpression(A operand1, BlockMetadata metadata) {
        super(operand1, metadata);
        this.operand1 = operand1;
        this.metadata = metadata;
    }

    @Override
    public BlockMetadata getMetadata() {
        return metadata;
    }

    public A getOperand1() {
        return operand1;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
