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
package de.uni_passau.fim.se2.litterbox.analytics.bugs.dataflow;

import de.uni_passau.fim.se2.litterbox.analytics.bugs.Type;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Answer;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.Color;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import java.util.function.Function;

public class TypeInference {

    public static Type inferType(Expression expr, Function<Identifier, Type> resolver) {
        if (expr instanceof Identifier id) {
             return resolver.apply(id);
        } else if (expr instanceof Answer) {
            return Type.ANY;
        } else if (expr instanceof NumberLiteral) {
            return Type.NUMBER;
        } else if (expr instanceof StringLiteral) {
            return Type.STRING;
        } else if (expr instanceof Color) {
            return Type.COLOR;
        } else if (expr instanceof BoolExpr) {
            return Type.BOOLEAN;
        } else if (expr instanceof NumExpr) {
            return Type.NUMBER;
        } else if (expr instanceof StringExpr) {
            return Type.STRING;
        }
        return null; // Implicit cast or unknown
    }

    // Default inference without context
    public static Type inferType(Expression expr) {
        return inferType(expr, id -> null);
    }
}
