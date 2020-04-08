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
package de.uni_passau.fim.se2.litterbox.ast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.parser.ExpressionParser;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class CallStmtParser {

    public static Stmt parse(JsonNode current, JsonNode blocks) throws ParsingException {
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
        Iterator<Entry<String, JsonNode>> entries = inputNode.fields();
        int i = 0;
        while (entries.hasNext()) {
            Entry<String, JsonNode> currentEntry = entries.next();
            if (arrayNodeContains(argumentsArray, currentEntry.getKey())) {
                expressions.add(ExpressionParser.parseExpressionWithPos(current, i, blocks));
            }
            i++;
        }

        return new CallStmt(new StrId(current.get(Constants.MUTATION_KEY).get(Constants.PROCCODE_KEY).asText()),
                new ExpressionList(expressions));
    }

    /**
     * Returns true if the array node contains a node which equals the key.
     *
     * @param node Array node to check.
     * @param key  The string existence of which will be checked.
     * @return True iff there is a TextNode the text of which equals the key.
     */
    private static boolean arrayNodeContains(ArrayNode node, String key) {
        Preconditions.checkNotNull(node);
        Preconditions.checkNotNull(key);
        Iterator<JsonNode> elements = node.elements();
        while (elements.hasNext()) {
            JsonNode next = elements.next();
            String nextText = next.asText();
            if (nextText != null) {
                if (nextText.equals(key)) {
                    return true;
                }
            }
        }
        return false;
    }
}
