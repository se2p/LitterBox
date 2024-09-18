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

import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.BlockRef;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawBlock;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawInput;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.ShadowType;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.NumExprOpcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.SpriteMotionStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;

final class PositionConverter {
    private PositionConverter() {
        throw new IllegalCallerException("utility class constructor");
    }

    static Position convertPosition(final ProgramParserState state, final RawBlock.RawRegularBlock block) {
        if (
                !block.inputs().containsKey(Constants.TO_KEY)
                && !block.inputs().containsKey(Constants.TOWARDS_KEY)
                && !block.inputs().containsKey(Constants.DISTANCETOMENU_KEY)
        ) {
            throw new InternalParsingException("Unknown position block.");
        }

        if (SpriteMotionStmtOpcode.motion_goto.name().equals(block.opcode())
                || SpriteMotionStmtOpcode.motion_glideto.name().equals(block.opcode())
        ) {
            return convertPositionInput(state, block, Constants.TO_KEY);
        } else if (SpriteMotionStmtOpcode.motion_pointtowards.name().equals(block.opcode())) {
            return convertPositionInput(state, block, Constants.TOWARDS_KEY);
        } else if (NumExprOpcode.sensing_distanceto.name().equals(block.opcode())) {
            return convertPositionInput(state, block, Constants.DISTANCETOMENU_KEY);
        } else {
            throw new InternalParsingException(
                    "Did not expect block type '" + block.opcode() + "' to contain a reference to a relative position."
            );
        }
    }

    private static Position convertPositionInput(
            final ProgramParserState state, final RawBlock.RawRegularBlock block, final String inputKey
    ) {
        final RawInput positionInput = block.inputs().get(inputKey);

        if (ShadowType.SHADOW.equals(positionInput.shadowType())
                && positionInput.input() instanceof BlockRef.IdRef menuIdRef
                && state.getBlock(menuIdRef.id()) instanceof RawBlock.RawRegularBlock positionBlock
        ) {
            final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(
                    menuIdRef.id(), positionBlock
            );
            final String positionName = positionBlock.fields().get(inputKey).value().toString();

            return switch (positionName) {
                case Constants.MOUSE -> new MousePos(metadata);
                case Constants.RANDOM -> new RandomPos(metadata);
                default -> new FromExpression(new AsString(new StrId(positionName)), metadata);
            };
        } else {
            final StringExpr expr = StringExprConverter.convertStringExpr(state, block, positionInput);
            return new FromExpression(expr, new NoBlockMetadata());
        }
    }
}
