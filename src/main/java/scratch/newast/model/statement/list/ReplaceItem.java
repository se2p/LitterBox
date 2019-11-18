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
package scratch.newast.model.statement.list;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.expression.string.StringExpr;
import scratch.newast.model.variable.Variable;

public class ReplaceItem implements ListStmt {

    private final NumExpr index;
    private final Variable variable;
    private final StringExpr string;
    private final ImmutableList<ASTNode> children;

    public ReplaceItem(StringExpr string, NumExpr index, Variable variable) {
        this.index = index;
        this.variable = variable;
        this.string = string;
        Builder<ASTNode> builder = ImmutableList.<ASTNode>builder();
        builder.add(index);
        builder.add(variable);
        builder.add(string);
        children = builder.build();
    }

    public NumExpr getIndex() {
        return index;
    }

    public Variable getVariable() {
        return variable;
    }

    public StringExpr getString() {
        return string;
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