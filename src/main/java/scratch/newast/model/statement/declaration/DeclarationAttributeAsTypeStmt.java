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
package scratch.newast.model.statement.declaration;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.string.StringExpr;
import scratch.newast.model.type.Type;

public class DeclarationAttributeAsTypeStmt implements DeclarationStmt{

    private StringExpr stringExpr;
    private Type type;
    private final ImmutableList<ASTNode> children;

    public DeclarationAttributeAsTypeStmt(StringExpr stringExpr, Type type) {
        this.stringExpr = stringExpr;
        this.type = type;
        ImmutableList.Builder<ASTNode> builder = ImmutableList.builder();
        children = builder.add(stringExpr).add(type).build();
    }

    public StringExpr getStringExpr() {
        return stringExpr;
    }

    public void setStringExpr(StringExpr stringExpr) {
        this.stringExpr = stringExpr;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
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
