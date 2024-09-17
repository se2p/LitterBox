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
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.ElementChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.*;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.*;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.SpriteLookStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;

final class SpriteLookStmtConverter extends StmtConverter<SpriteLookStmt> {

    SpriteLookStmtConverter(final ProgramParserState state) {
        super(state);
    }

    @Override
    SpriteLookStmt convertStmt(final RawBlockId blockId, final RawBlock.RawRegularBlock block) {
        final SpriteLookStmtOpcode opcode = SpriteLookStmtOpcode.valueOf(block.opcode());
        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(blockId, block);

        return switch (opcode) {
            case looks_show -> new Show(metadata);
            case looks_hide -> new Hide(metadata);
            case looks_sayforsecs -> {
                final StringExpr message = StringExprConverter.convertStringExpr(
                        state, block, block.inputs().get(Constants.MESSAGE_KEY)
                );
                final NumExpr secs = NumExprConverter.convertNumExpr(
                        state, block, block.inputs().get(Constants.SECS_KEY)
                );
                yield new SayForSecs(message, secs, metadata);
            }
            case looks_say -> {
                final StringExpr message = StringExprConverter.convertStringExpr(
                        state, block, block.inputs().get(Constants.MESSAGE_KEY)
                );
                yield new Say(message, metadata);
            }
            case looks_thinkforsecs -> {
                final StringExpr message = StringExprConverter.convertStringExpr(
                        state, block, block.inputs().get(Constants.MESSAGE_KEY)
                );
                final NumExpr secs = NumExprConverter.convertNumExpr(
                        state, block, block.inputs().get(Constants.SECS_KEY)
                );
                yield new ThinkForSecs(message, secs, metadata);
            }
            case looks_think -> {
                final StringExpr message = StringExprConverter.convertStringExpr(
                        state, block, block.inputs().get(Constants.MESSAGE_KEY)
                );
                yield new Think(message, metadata);
            }
            case looks_switchcostumeto -> {
                final ElementChoice costumeChoice = convertCostumeChoice(block);
                yield new SwitchCostumeTo(costumeChoice, metadata);
            }
            case looks_nextcostume -> new NextCostume(metadata);
            case looks_changesizeby -> {
                final NumExpr size = NumExprConverter.convertNumExpr(
                        state, block, block.inputs().get(Constants.CHANGE_KEY)
                );
                yield new ChangeSizeBy(size, metadata);
            }
            case looks_setsizeto -> {
                final NumExpr size = NumExprConverter.convertNumExpr(
                        state, block, block.inputs().get(Constants.SIZE_KEY_CAP)
                );
                yield new SetSizeTo(size, metadata);
            }
            case looks_gotofrontback -> {
                final String option = block.fields().get("FRONT_BACK").value().toString();
                final LayerChoice choice = new LayerChoice(option);
                yield new GoToLayer(choice, metadata);
            }
            case looks_goforwardbackwardlayers -> {
                final String layerOption =  block.fields().get("FORWARD_BACKWARD").value().toString();
                final ForwardBackwardChoice choice = new ForwardBackwardChoice(layerOption);
                final NumExpr numExpr = NumExprConverter.convertNumExpr(
                        state, block, block.inputs().get(Constants.NUM_KEY)
                );
                yield new ChangeLayerBy(numExpr, choice, metadata);
            }
        };
    }

    private ElementChoice convertCostumeChoice(final RawBlock.RawRegularBlock block) {
        final RawInput costumeInput = block.inputs().get(Constants.COSTUME_INPUT);

        if (
                ShadowType.SHADOW.equals(costumeInput.shadowType())
                && costumeInput.input() instanceof BlockRef.IdRef blockIdRef
        ) {
            return convertCostumeChoiceMenu(blockIdRef.id());
        } else {
            final BlockMetadata metadata = new NoBlockMetadata();
            final Expression expr = ExpressionConverter.convertExpr(state, block, costumeInput);
            return new WithExpr(expr, metadata);
        }
    }

    private ElementChoice convertCostumeChoiceMenu(final RawBlockId menuRef) {
        final RawBlock menu = state.getBlock(menuRef);
        if (!(menu instanceof RawBlock.RawRegularBlock menuBlock)) {
            throw new InternalParsingException("Unknown menu representation.");
        }

        final String elementName = menuBlock.fields().get(Constants.COSTUME_INPUT).value().toString();
        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(menuRef, menu);

        return new WithExpr(new StrId(elementName), metadata);
    }
}
