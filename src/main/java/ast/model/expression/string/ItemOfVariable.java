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
package ast.model.expression.string;

import ast.model.AbstractNode;
import ast.model.expression.bool.BoolExpr;
import ast.model.expression.num.NumExpr;
import ast.model.variable.Variable;
import ast.visitor.ScratchVisitor;

public class ItemOfVariable extends AbstractNode implements StringExpr, BoolExpr {

    private final NumExpr num;
    private final Variable variable;

    public ItemOfVariable(NumExpr num, Variable variable) {
        super(num, variable);
        this.num = num;
        this.variable = variable;
    }

    public NumExpr getNum() {
        return num;
    }

    public Variable getVariable() {
        return variable;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

}