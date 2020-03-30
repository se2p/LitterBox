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
package de.uni_passau.fim.se2.litterbox.ast.model.touchable.color;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class Rgba extends AbstractNode implements Color {

    private NumExpr rValue;
    private NumExpr gValue;
    private NumExpr bValue;
    private NumExpr aValue;

    public Rgba(NumExpr rValue, NumExpr gValue, NumExpr bValue, NumExpr aValue) {
        super(rValue, gValue, bValue, aValue);
        this.rValue = Preconditions.checkNotNull(rValue);
        this.gValue = Preconditions.checkNotNull(gValue);
        this.bValue = Preconditions.checkNotNull(bValue);
        this.aValue = Preconditions.checkNotNull(aValue);
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
}
