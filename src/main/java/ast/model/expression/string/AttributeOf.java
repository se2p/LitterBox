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
import ast.model.variable.Identifier;
import ast.visitor.ScratchVisitor;

public class AttributeOf extends AbstractNode implements StringExpr {

    private final StringExpr attribute;
    private final Identifier identifier;

    public AttributeOf(StringExpr attribute, Identifier identifier) {
        super(attribute, identifier);
        this.attribute = attribute;
        this.identifier = identifier;
    }

    public StringExpr getAttribute() {
        return attribute;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

}