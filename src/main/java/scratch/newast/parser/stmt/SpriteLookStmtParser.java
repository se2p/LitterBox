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
package scratch.newast.parser.stmt;

import static scratch.newast.Constants.OPCODE_KEY;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import scratch.newast.ParsingException;
import scratch.newast.model.elementchoice.ElementChoice;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.expression.string.StringExpr;
import scratch.newast.model.statement.spritelook.ChangeSizeBy;
import scratch.newast.model.statement.spritelook.Hide;
import scratch.newast.model.statement.spritelook.Say;
import scratch.newast.model.statement.spritelook.SayForSecs;
import scratch.newast.model.statement.spritelook.SetSizeTo;
import scratch.newast.model.statement.spritelook.Show;
import scratch.newast.model.statement.spritelook.SpriteLookStmt;
import scratch.newast.model.statement.spritelook.SwitchCostumeTo;
import scratch.newast.model.statement.spritelook.Think;
import scratch.newast.model.statement.spritelook.ThinkForSecs;
import scratch.newast.opcodes.SpriteLookStmtOpcode;
import scratch.newast.parser.ElementChoiceParser;
import scratch.newast.parser.NumExprParser;
import scratch.newast.parser.StringExprParser;

public class SpriteLookStmtParser {

    public static SpriteLookStmt parse(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
            .checkArgument(SpriteLookStmtOpcode.contains(opcodeString),
                "Given blockID does not point to a sprite look block. Opcode is " + opcodeString);

        SpriteLookStmtOpcode opcode = SpriteLookStmtOpcode.valueOf(opcodeString);
        StringExpr stringExpr;
        NumExpr numExpr;

        switch (opcode) {
            case looks_show:
                return new Show();
            case looks_hide:
                return new Hide();
            case looks_sayforsecs:
                stringExpr = StringExprParser.parseStringExpr(current, 0, allBlocks);
                numExpr = NumExprParser.parseNumExpr(current, 1, allBlocks);
                return new SayForSecs(stringExpr, numExpr);
            case looks_say:
                stringExpr = StringExprParser.parseStringExpr(current, 0, allBlocks);
                return new Say(stringExpr);
            case looks_thinkforsecs:
                stringExpr = StringExprParser.parseStringExpr(current, 0, allBlocks);
                numExpr = NumExprParser.parseNumExpr(current, 1, allBlocks);
                return new ThinkForSecs(stringExpr, numExpr);
            case looks_think:
                stringExpr = StringExprParser.parseStringExpr(current, 0, allBlocks);
                return new Think(stringExpr);
            case looks_switchcostumeto:
                ElementChoice choice = ElementChoiceParser.parse(current, allBlocks);
                return new SwitchCostumeTo(choice);
            case looks_changesizeby:
                numExpr = NumExprParser.parseNumExpr(current, 0, allBlocks);
                return new ChangeSizeBy(numExpr);
            case looks_setsizeto:
                numExpr = NumExprParser.parseNumExpr(current, 0, allBlocks);
                return new SetSizeTo(numExpr);
            case looks_gotofrontback:
            case looks_goforwardbackwardlayers:
            default:
                throw new RuntimeException("Not implemented for opcode " + opcodeString);
        }
    }
}
