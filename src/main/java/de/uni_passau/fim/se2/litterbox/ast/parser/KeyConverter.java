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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import de.uni_passau.fim.se2.litterbox.ast.model.Key;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.BoolExprOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast.*;

final class KeyConverter {

    private KeyConverter() {
        throw new IllegalCallerException("utility class constructor");
    }

    static Key convertKey(final ProgramParserState state, final RawBlock keyBlock) {
        if (!(keyBlock instanceof RawBlock.RawRegularBlock actualKeyBlock)) {
            throw new InternalParsingException("Cannot parse key without opcode!");
        }

        if (BoolExprOpcode.sensing_keypressed.name().equals(actualKeyBlock.opcode())) {
            final RawInput keyInput = actualKeyBlock.getInput(KnownInputs.KEY_OPTION);

            if (ShadowType.SHADOW == keyInput.shadowType()
                    && keyInput.input() instanceof BlockRef.IdRef(RawBlockId menuId)
                    && state.getBlock(menuId) instanceof RawBlock.RawRegularBlock menuBlock
            ) {
                final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(menuId, menuBlock);
                final String keyValue = menuBlock.getFieldValueAsString(KnownFields.KEY_OPTION);

                return convertKey(keyValue, metadata);
            } else {
                // if there is a variable or expression, we evaluate it and use it as key
                final NumExpr numExpr = NumExprConverter.convertNumExpr(state, actualKeyBlock, keyInput);
                return new Key(numExpr, new NoBlockMetadata());
            }
        } else {
            final BlockMetadata metadata = new NoBlockMetadata();
            final String keyValue = actualKeyBlock.getFieldValueAsString(KnownFields.KEY_OPTION);

            return convertKey(keyValue, metadata);
        }
    }

    private static Key convertKey(final String keyValue, final BlockMetadata metadata) {
        return switch (keyValue) {
            case "space" -> new Key(new NumberLiteral(KeyCode.SPACE.getKeycode()), metadata);
            case "up arrow" -> new Key(new NumberLiteral(KeyCode.UP_ARROW.getKeycode()), metadata);
            case "down arrow" -> new Key(new NumberLiteral(KeyCode.DOWN_ARROW.getKeycode()), metadata);
            case "left arrow" -> new Key(new NumberLiteral(KeyCode.LEFT_ARROW.getKeycode()), metadata);
            case "right arrow" -> new Key(new NumberLiteral(KeyCode.RIGHT_ARROW.getKeycode()), metadata);
            case "any" -> new Key(new NumberLiteral(KeyCode.ANY_KEY.getKeycode()), metadata);
            default -> {
                if (!keyValue.isEmpty()) {
                    yield new Key(new NumberLiteral(keyValue.charAt(0)), metadata);
                } else {
                    // It is not clear how this can happen, but it happens sometimes.
                    yield new Key(new NumberLiteral(0), metadata);
                }
            }
        };
    }
}
