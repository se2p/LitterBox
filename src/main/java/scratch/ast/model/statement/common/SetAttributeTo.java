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

import com.google.common.collect.ImmutableList;
import scratch.ast.model.ASTNode;
import scratch.ast.model.ScratchVisitor;
import scratch.ast.model.expression.Expression;
import scratch.ast.model.expression.string.StringExpr;

public class SetAttributeTo implements SetStmt {

    private final ImmutableList<ASTNode> children;
    private StringExpr stringExpr;
    private Expression expr;

    public SetAttributeTo(StringExpr stringExpr, Expression expr) {
        this.stringExpr = stringExpr;
        this.expr = expr;
        children = ImmutableList.<ASTNode>builder().add(stringExpr).add(expr).build();
    }

    public StringExpr getStringExpr() {
        return stringExpr;
    }

    public Expression getExpr() {
        return expr;
    }

    public void setExpr(Expression expr) {
        this.expr = expr;
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