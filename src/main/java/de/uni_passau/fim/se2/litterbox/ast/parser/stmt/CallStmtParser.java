/*
 * Copyright (C) 2020 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.parser.ExpressionParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.BlockMetadataParser;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class CallStmtParser {

    /**
     * This method parses the call to a procedure.
     *
     * @param identifier the id of the call block in the JSON.
     * @param current    the call block in the JSON.
     * @param blocks     the whole blocks node of the current Actor.
     * @return the constructed CallStmt
     * @throws ParsingException when a call is malformated. This can be the case when the call has more
     *                          argument ids defined than it has inputs or the parsing of used expressions
     *                          does not work.
     */
    public static CallStmt parse(String identifier, JsonNode current, JsonNode blocks) throws ParsingException {
        List<Expression> expressions = new ArrayList<>();
        JsonNode argumentIds = current.get(MUTATION_KEY).get(ARGUMENTIDS_KEY);
        ObjectMapper mapper = new ObjectMapper();

        final JsonNode argumentsNode;
        try {
            argumentsNode = mapper.readTree(argumentIds.asText());
        } catch (IOException e) {
            throw new ParsingException("Could not read argument names of a procedure");
        }

        Preconditions.checkArgument(argumentsNode.isArray());
        ArrayNode argumentsArray = (ArrayNode) argumentsNode;
        JsonNode inputNode = current.get(INPUTS_KEY);
        for (JsonNode id :
                argumentsArray) {
            if (inputNode.has(id.asText())) {
                expressions.add(ExpressionParser.parseExpr(current, id.asText(), blocks));
            } else {
                expressions.add(new UnspecifiedBoolExpr());
            }
        }

        return new CallStmt(new StrId(current.get(Constants.MUTATION_KEY).get(Constants.PROCCODE_KEY).asText()),
                new ExpressionList(expressions), BlockMetadataParser.parse(identifier, current));
    }
}
