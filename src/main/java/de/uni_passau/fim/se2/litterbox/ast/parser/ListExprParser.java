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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.UnspecifiedId;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;

public class ListExprParser {

    /**
     * Returns true iff the specified input of the block can be parsed as
     * ListExpr.
     *
     * @param containingBlock The block inputs of which contain the expression
     *                        to be checked.
     * @param inputKey        The key of the input containing the expression to be checked.
     * @return True iff the the input of the containing block is parsable as ListExpr.
     */
    public static boolean parsableAsListExpr(JsonNode containingBlock, String inputKey) {
        ArrayNode exprArray = ExpressionParser.getExprArray(containingBlock.get(INPUTS_KEY), inputKey);
        if (ExpressionParser.getShadowIndicator(exprArray) == 1 || exprArray.get(POS_BLOCK_ID) instanceof TextNode) {
            return false;
        } else {
            String idString = exprArray.get(POS_DATA_ARRAY).get(POS_INPUT_ID).asText();
            return ProgramParser.symbolTable.getLists().containsKey(idString);
        }
    }


    static Identifier parseVariableFromFields(JsonNode fields) throws ParsingException { //TODO do we need this?
        String identifier = fields.get(LIST_KEY).get(LIST_IDENTIFIER_POS).asText();
        if (ProgramParser.symbolTable.getLists().containsKey(identifier)) {
            ExpressionListInfo variableInfo = ProgramParser.symbolTable.getLists().get(identifier);
            return new Qualified(new StrId(variableInfo.getActor()),
                    new ScratchList(new StrId((variableInfo.getVariableName()))));
        } else {
            return new UnspecifiedId();
        }
    }
}
