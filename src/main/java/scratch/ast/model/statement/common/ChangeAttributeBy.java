/*
 * Copyright (C) 2019 LitterBox contributors
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
package scratch.ast.model.statement.common;

import scratch.utils.UnmodifiableListBuilder;
import com.google.common.base.Preconditions;
import scratch.ast.model.ASTNode;
import scratch.ast.model.expression.Expression;
import scratch.ast.model.expression.string.StringExpr;
import scratch.ast.model.variable.Variable;
import scratch.ast.visitor.ScratchVisitor;

import javax.annotation.Nonnull;

public class ChangeAttributeBy implements CommonStmt {

    private final ImmutableList<ASTNode> children;
    private final StringExpr attribute;
    private final Expression expr;

    public ChangeAttributeBy(@Nonnull StringExpr attribute, @Nonnull Expression expr) {
        this.attribute = Preconditions.checkNotNull(attribute);
        this.expr = Preconditions.checkNotNull(expr);
        this.children = ImmutableList.<ASTNode>builder().add(attribute).add(expr).build();
    }

    public StringExpr getAttribute() {
        return attribute;
    }

    public Expression getExpr() {
        return expr;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ImmutableList<ASTNode> getChildren() {
        return children;
    }
}