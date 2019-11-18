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
package scratch.newast.model.color;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.num.NumExpr;

public class Rgba implements Color {

    private final ImmutableList<ASTNode> children;
    private NumExpr rValue;
    private NumExpr gValue;
    private NumExpr bValue;
    private NumExpr aValue;

    public Rgba(NumExpr rValue, NumExpr gValue, NumExpr bValue, NumExpr aValue) {
        this.rValue = rValue;
        this.gValue = gValue;
        this.bValue = bValue;
        this.aValue = aValue;
        children = ImmutableList.<ASTNode>builder()
            .add(rValue)
            .add(gValue)
            .add(bValue)
            .add(aValue)
            .build();
    }

    public NumExpr getrValue() {
        return rValue;
    }

    public NumExpr getgValue() {
        return gValue;
    }

    public NumExpr getbValue() {
        return bValue;
    }

    public NumExpr getaValue() {
        return aValue;
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
