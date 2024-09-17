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
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.MutationMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.ProcedureMutationMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.Touchable;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.Color;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.FromNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.BlockRef;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawBlock;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawInput;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawMutation;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;

/**
 * Groups various small helper functions for the converters that are not worth having their own class.
 */
final class ConverterUtilities {
    private ConverterUtilities() {
        throw new IllegalCallerException("utility class constructor");
    }

    static Qualified listInfoToIdentifier(final ExpressionListInfo listInfo, final String listName) {
        return new Qualified(new StrId(listInfo.getActor()), new ScratchList(new StrId(listName)));
    }

    static MutationMetadata convertMutation(final RawMutation mutation) {
        return new ProcedureMutationMetadata(mutation.warp());
    }

    static Touchable convertTouchable(final ProgramParserState state, final RawBlock block) {
        throw new UnsupportedOperationException("todo: touchables");
    }

    static Color convertColor(
            final ProgramParserState state, final RawBlock.RawRegularBlock containingBlock, final RawInput block
    ) {
        // FIXME parse inputs that are not a text color as a "FromNumber" color

        if (block.input() instanceof BlockRef.Block dataBlock
                && dataBlock.block() instanceof RawBlock.RawColorLiteral color
                && color.color().startsWith("#")) {
            final long r = Long.parseLong(color.color().substring(1, 3), 16);
            final long g = Long.parseLong(color.color().substring(3, 5), 16);
            final long b = Long.parseLong(color.color().substring(5, 7), 16);

            return new ColorLiteral(r, g, b);
        } else {
            final NumExpr numExpr = NumExprConverter.convertNumExpr(state, containingBlock, block);
            return new FromNumber(numExpr);
        }
    }
}
