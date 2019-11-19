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
package scratch.ast.parser.stmt;

import static scratch.ast.Constants.INPUTS_KEY;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import scratch.ast.ParsingException;
import scratch.ast.model.expression.Expression;
import scratch.ast.model.expression.list.ExpressionList;
import scratch.ast.model.expression.list.ExpressionListPlain;
import scratch.ast.model.statement.CallStmt;
import scratch.ast.model.statement.Stmt;
import scratch.ast.model.variable.StrId;
import scratch.ast.parser.ExpressionParser;

public class CallStmtParser {

    public static Stmt parse(JsonNode current, String blockId, JsonNode blocks) throws ParsingException {
        List<Expression> expressions = new ArrayList<>();
        JsonNode inputNode = current.get(INPUTS_KEY);
        for (int i = 0; i < inputNode.size(); i++) {
            expressions.add(ExpressionParser.parseExpression(current,i,blocks));
        }

        return new CallStmt(new StrId(blockId), new ExpressionList(new ExpressionListPlain(expressions)));
    }
}
