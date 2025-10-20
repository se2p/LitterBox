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

import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.*;
import de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast.Coordinates;
import de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast.RawBlock;
import de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast.RawBlockId;

import java.util.Optional;

final class RawBlockMetadataConverter {
    private RawBlockMetadataConverter() {
        throw new IllegalCallerException("utility class constructor");
    }

    static BlockMetadata convertBlockMetadata(final RawBlockId blockId, final RawBlock block) {
        return convertBlockMetadata(blockId, block, false);
    }

    static BlockMetadata convertTopLevelBlockMetadata(final RawBlockId blockId, final RawBlock block) {
        return convertBlockMetadata(blockId, block, true);
    }

    private static BlockMetadata convertBlockMetadata(
            final RawBlockId blockId, final RawBlock block, final boolean isTopLevel
    ) {
        return switch (block) {
            case RawBlock.RawRegularBlock regularBlock -> convertRegularBlockMetadata(blockId, regularBlock);
            case RawBlock.ArrayBlock arrayBlock -> convertArrayBlockMetadata(blockId, arrayBlock, isTopLevel);
        };
    }

    private static BlockMetadata convertRegularBlockMetadata(
            final RawBlockId id, final RawBlock.RawRegularBlock block
    ) {
        final String commentId = block.comment().map(RawBlockId::id).orElse(null);
        final MutationMetadata mutation = block.mutation()
                .map(ConverterUtilities::convertMutation)
                .orElse(new NoMutationMetadata());

        if (!block.topLevel()) {
            return new NonDataBlockMetadata(commentId, id.id(), block.shadow(), mutation);
        } else {
            return new TopNonDataBlockMetadata(commentId, id.id(), block.shadow(), mutation, block.x(), block.y());
        }
    }

    private static BlockMetadata convertArrayBlockMetadata(
            final RawBlockId id, final RawBlock.ArrayBlock block, final boolean isTopLevel
    ) {
        if (!isTopLevel) {
            return new NoBlockMetadata();
        }

        final Optional<Coordinates> coordinates;
        final String commentId;

        if (block instanceof RawBlock.RawVariable variable) {
            coordinates = variable.coordinates();
            commentId = variable.comment().map(RawBlockId::id).orElse(null);
        } else if (block instanceof RawBlock.RawList list) {
            coordinates = list.coordinates();
            commentId = list.comment().map(RawBlockId::id).orElse(null);
        } else {
            throw new InternalParsingException(
                    "Did not expect to parse metadata for a literal block! Something is wrong."
            );
        }

        // non-top-level blocks do not have coordinates => since they are not visible, just use (0,0)
        // not sure when this case can even happen, might have been a bug in the Scratch-VM when deleting
        // scripts, or when converting from Scratch 2 to Scratch 3?
        final double x = coordinates.map(Coordinates::x).orElse(0.0);
        final double y = coordinates.map(Coordinates::y).orElse(0.0);

        return new DataBlockMetadata(id.id(), commentId, x, y);
    }
}
