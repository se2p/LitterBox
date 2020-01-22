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
package ast.model.statement.list;

import ast.model.AbstractNode;
import ast.model.expression.num.NumExpr;
import ast.model.expression.string.StringExpr;
import ast.model.variable.Variable;
import ast.visitor.ScratchVisitor;
import utils.Preconditions;

public class InsertAt extends AbstractNode implements ListStmt {

    private final StringExpr string;
    private final NumExpr index;
    private final Variable variable;

    public InsertAt(StringExpr string, NumExpr index, Variable variable) {
        super(string, index , variable);
        this.string = Preconditions.checkNotNull(string);
        this.index = Preconditions.checkNotNull(index);
        this.variable = Preconditions.checkNotNull(variable);
    }

    public StringExpr getString() {
        return string;
    }

    public NumExpr getIndex() {
        return index;
    }

    public Variable getVariable() {
        return variable;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

}