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
package scratch.ast.model.statement.pen;

import scratch.ast.model.AbstractNode;
import scratch.ast.model.expression.num.NumExpr;
import scratch.ast.model.expression.string.StringExpr;
import scratch.ast.visitor.ScratchVisitor;

public class SetPenColorParamTo extends AbstractNode implements PenStmt {
    private NumExpr value;
    private StringExpr param;

    public SetPenColorParamTo(NumExpr value, StringExpr param) {
        super(value, param);
        this.value = value;
        this.param = param;
    }

    public NumExpr getValue() {
        return value;
    }

    public StringExpr getParam() {
        return param;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
