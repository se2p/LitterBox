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
package scratch.ast.model.statement.declaration;

import scratch.ast.model.AbstractNode;
import scratch.utils.UnmodifiableListBuilder;
import com.google.common.base.Preconditions;
import scratch.ast.model.ASTNode;
import scratch.ast.model.expression.string.StringExpr;
import scratch.ast.model.type.Type;
import scratch.ast.visitor.ScratchVisitor;

public class DeclarationAttributeAsTypeStmt extends AbstractNode implements DeclarationStmt{

    private final StringExpr stringExpr;
    private final Type type;

    public DeclarationAttributeAsTypeStmt(StringExpr stringExpr, Type type) {
        super(stringExpr, type);
        this.stringExpr = Preconditions.checkNotNull(stringExpr);
        this.type = Preconditions.checkNotNull(type);
    }

    public StringExpr getStringExpr() {
        return stringExpr;
    }

    public Type getType() {
        return type;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

}
