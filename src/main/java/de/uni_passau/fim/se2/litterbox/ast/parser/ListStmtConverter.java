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

import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.*;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.ListStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast.KnownInputs;
import de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast.RawBlock;
import de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast.RawBlockId;

final class ListStmtConverter extends StmtConverter<ListStmt> {
    ListStmtConverter(ProgramParserState state) {
        super(state);
    }

    @Override
    ListStmt convertStmt(final RawBlockId blockId, final RawBlock.RawRegularBlock block) {
        final ListStmtOpcode opcode = ListStmtOpcode.valueOf(block.opcode());
        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(blockId, block);

        return switch (opcode) {
            case data_replaceitemoflist -> {
                final StringExpr newValue = StringExprConverter.convertStringExpr(state, block, KnownInputs.ITEM);
                final NumExpr location = NumExprConverter.convertNumExpr(state, block, KnownInputs.INDEX);
                final Qualified list = getOrCreateReferencedList(block);

                yield new ReplaceItem(newValue, location, list, metadata);
            }
            case data_insertatlist -> {
                final StringExpr inserted = StringExprConverter.convertStringExpr(state, block, KnownInputs.ITEM);
                final NumExpr location = NumExprConverter.convertNumExpr(state, block, KnownInputs.INDEX);
                final Qualified list = getOrCreateReferencedList(block);

                yield new InsertAt(inserted, location, list, metadata);
            }
            case data_deletealloflist -> {
                final Qualified list = getOrCreateReferencedList(block);
                yield new DeleteAllOf(list, metadata);
            }
            case data_deleteoflist -> {
                final NumExpr expr = NumExprConverter.convertNumExpr(state, block, KnownInputs.INDEX);
                final Qualified list = getOrCreateReferencedList(block);

                yield new DeleteOf(expr, list, metadata);
            }
            case data_addtolist -> {
                final StringExpr expr = StringExprConverter.convertStringExpr(state, block, KnownInputs.ITEM);
                final Qualified list = getOrCreateReferencedList(block);

                yield new AddTo(expr, list, metadata);
            }
        };
    }
}
