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
package ast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import ast.Constants;
import ast.ParsingException;
import ast.model.expression.Expression;
import ast.model.expression.list.ExpressionList;
import ast.model.expression.list.ExpressionListPlain;
import ast.model.statement.CallStmt;
import ast.model.statement.Stmt;
import ast.model.variable.StrId;
import ast.parser.ExpressionParser;

import java.util.ArrayList;
import java.util.List;

import static ast.Constants.INPUTS_KEY;

public class CallStmtParser {

    public static Stmt parse(JsonNode current, JsonNode blocks) throws ParsingException {
        List<Expression> expressions = new ArrayList<>();
        JsonNode inputNode = current.get(INPUTS_KEY);
        for (int i = 0; i < inputNode.size(); i++) {
            expressions.add(ExpressionParser.parseExpression(current,i,blocks));
        }

        return new CallStmt(new StrId(current.get(Constants.MUTATION_KEY).get(Constants.PROCCODE_KEY).asText()), new ExpressionList(new ExpressionListPlain(expressions)));
    }
}
