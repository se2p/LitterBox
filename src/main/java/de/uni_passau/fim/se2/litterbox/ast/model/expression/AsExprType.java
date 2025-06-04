/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.model.expression;

import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;

/**
 * A wrapper around other expressions to make the types match.
 *
 * <p>The Scratch programming language does not enforce strict typing.
 * E.g., it is possible to use number literals as operands in blocks that
 * accept strings.
 * In our AST representation the block argument typing is more strict, i.e.,
 * expressions are subclassed into boolean, numeric, and string expressions.
 * To be able to represent all Scratch programs, this wrapper can be used to
 * make for example a numeric expression look like a string expression.
 */
public abstract class AsExprType extends UnaryExpression<Expression> {
    protected AsExprType(Expression operand1, BlockMetadata metadata) {
        super(operand1, metadata);
    }
}
