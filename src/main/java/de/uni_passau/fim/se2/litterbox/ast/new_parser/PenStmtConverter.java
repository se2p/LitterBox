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
package de.uni_passau.fim.se2.litterbox.ast.new_parser;

import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.*;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.Color;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.*;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.DependentBlockOpcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.PenOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;

final class PenStmtConverter extends StmtConverter<PenStmt> {

    PenStmtConverter(ProgramParserState state) {
        super(state);
    }

    @Override
    PenStmt convertStmt(final RawBlockId blockId, final RawBlock.RawRegularBlock block) {
        final PenOpcode opcode = PenOpcode.valueOf(block.opcode());
        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(blockId, block);

        return switch (opcode) {
            case pen_clear -> new PenClearStmt(metadata);
            case pen_stamp -> new PenStampStmt(metadata);
            case pen_penDown -> new PenDownStmt(metadata);
            case pen_penUp -> new PenUpStmt(metadata);
            case pen_setPenColorToColor -> {
                final Color color = ConverterUtilities.convertColor(state, block, block.getInput(KnownInputs.COLOR));
                yield new SetPenColorToColorStmt(color, metadata);
            }
            case pen_setPenColorParamTo -> {
                final NumExpr to = NumExprConverter.convertNumExpr(state, block, KnownInputs.VALUE);
                final ParamWithMetadata param = convertParam(block);
                final BlockMetadata metadataWithParam = RawBlockMetadataConverter.convertBlockWithMenuMetadata(
                        blockId, block, param.metadata()
                );

                yield new SetPenColorParamTo(to, param.param(), metadataWithParam);
            }
            case pen_changePenColorParamBy -> {
                final NumExpr by = NumExprConverter.convertNumExpr(state, block, KnownInputs.VALUE);
                final ParamWithMetadata param = convertParam(block);
                final BlockMetadata metadataWithParam = RawBlockMetadataConverter.convertBlockWithMenuMetadata(
                        blockId, block, param.metadata()
                );

                yield new ChangePenColorParamBy(by, param.param(), metadataWithParam);
            }
            case pen_setPenSizeTo -> {
                final NumExpr to = NumExprConverter.convertNumExpr(state, block, KnownInputs.SIZE);
                yield new SetPenSizeTo(to, metadata);
            }
            case pen_changePenSizeBy -> {
                final NumExpr by = NumExprConverter.convertNumExpr(state, block, KnownInputs.SIZE);
                yield new ChangePenSizeBy(by, metadata);
            }
        };
    }

    private ParamWithMetadata convertParam(final RawBlock.RawRegularBlock block) {
        final RawInput colorParamInput = block.getInput(KnownInputs.COLOR_PARAM);

        if (
                ShadowType.SHADOW.equals(colorParamInput.shadowType())
                && colorParamInput.input() instanceof BlockRef.IdRef colorParamMenuRef
                && state.getBlock(colorParamMenuRef.id()) instanceof RawBlock.RawRegularBlock menuBlock
                && DependentBlockOpcode.pen_menu_colorParam.getName().equals(menuBlock.opcode())
        ) {
            final String colorField = menuBlock.getFieldValueAsString(KnownFields.COLOR_PARAM);
            final StringExpr color = new StringLiteral(colorField);
            final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(
                    colorParamMenuRef.id(), menuBlock
            );

            return new ParamWithMetadata(color, metadata);
        } else {
            final StringExpr param = StringExprConverter.convertStringExpr(state, block, colorParamInput);
            return new ParamWithMetadata(param, new NoBlockMetadata());
        }
    }

    private record ParamWithMetadata(StringExpr param, BlockMetadata metadata) {
    }
}
