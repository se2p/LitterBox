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
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.PenWithParamMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.*;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.DependentBlockOpcodes;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.PenOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.ColorParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.NumExprParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.StringExprParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.BlockMetadataParser;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class PenStmtParser {
    private static BlockMetadata paramMetadata = new NoBlockMetadata();

    public static Stmt parse(String blockId, JsonNode current, JsonNode blocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(blocks);
        final String opCodeString = current.get(Constants.OPCODE_KEY).asText();
        if (!PenOpcode.contains(opCodeString)) {
            throw new ParsingException(
                    "Called parsePenStmt with a block that does not qualify as such"
                            + " a statement. Opcode is " + opCodeString);
        }
        final PenOpcode opcode = PenOpcode.valueOf(opCodeString);
        BlockMetadata metadata = BlockMetadataParser.parse(blockId, current);
        switch (opcode) {
            case pen_clear:
                return new PenClearStmt(metadata);
            case pen_penDown:
                return new PenDownStmt(metadata);
            case pen_penUp:
                return new PenUpStmt(metadata);
            case pen_stamp:
                return new PenStampStmt(metadata);
            case pen_setPenColorToColor:
                return new SetPenColorToColorStmt(ColorParser.parseColor(current, COLOR_KEY, blocks), metadata);
            case pen_changePenColorParamBy:
                NumExpr numExpr = NumExprParser.parseNumExpr(current, VALUE_KEY, blocks);
                StringExpr param = parseParam(current, blocks);
                return new ChangePenColorParamBy(numExpr, param, new PenWithParamMetadata(metadata, paramMetadata));
            case pen_setPenColorParamTo:
                numExpr = NumExprParser.parseNumExpr(current, VALUE_KEY, blocks);
                param = parseParam(current, blocks);
                return new SetPenColorParamTo(numExpr, param, new PenWithParamMetadata(metadata, paramMetadata));
            case pen_setPenSizeTo:
                return parseSetPenSizeTo(current, blocks, metadata);
            case pen_changePenSizeBy:
                return new ChangePenSizeBy(NumExprParser.parseNumExpr(current, SIZE_KEY_CAP,
                        blocks), metadata);
            default:
                throw new RuntimeException("Not implemented yet for opcode " + opcode);
        }
    }

    private static StringExpr parseParam(JsonNode current, JsonNode blocks) throws ParsingException {
        List<JsonNode> inputsList = new ArrayList<>();
        current.get(Constants.INPUTS_KEY).elements().forEachRemaining(inputsList::add);

        StringExpr expr;
        if (getShadowIndicator((ArrayNode) inputsList.get(0)) == 1) {
            String reference = current.get(INPUTS_KEY).get(COLOR_PARAM_BIG_KEY).get(POS_INPUT_VALUE).asText();
            JsonNode referredBlock = blocks.get(reference);
            Preconditions.checkNotNull(referredBlock);
            if (referredBlock.get(OPCODE_KEY).asText().equals(DependentBlockOpcodes.pen_menu_colorParam.name())) {
                JsonNode colorParamNode = referredBlock.get(FIELDS_KEY).get(COLOR_PARAM_LITTLE_KEY);
                Preconditions.checkArgument(colorParamNode.isArray());
                String attribute = colorParamNode.get(FIELD_VALUE).asText();
                expr = new StringLiteral(attribute);
                paramMetadata = BlockMetadataParser.parse(reference, referredBlock);
            } else {
                expr = StringExprParser.parseStringExpr(current, COLOR_PARAM_BIG_KEY, blocks);
                paramMetadata = new NoBlockMetadata();
            }
        } else {
            expr = StringExprParser.parseStringExpr(current, COLOR_PARAM_BIG_KEY, blocks);
            paramMetadata = new NoBlockMetadata();
        }

        return expr;
    }

    static int getShadowIndicator(ArrayNode exprArray) {
        return exprArray.get(Constants.POS_INPUT_SHADOW).asInt();
    }

    private static PenStmt parseSetPenSizeTo(JsonNode current, JsonNode allBlocks, BlockMetadata metadata)
            throws ParsingException {
        return new SetPenSizeTo(NumExprParser.parseNumExpr(current, "SIZE",
                allBlocks), metadata);
    }
}
