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

import de.uni_passau.fim.se2.litterbox.ast.model.Message;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.*;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.CommonStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast.*;

import java.util.Optional;

final class CommonStmtConverter extends StmtConverter<CommonStmt> {

    private static final String STOP_OTHER = "other scripts in sprite";
    private static final String STOP_OTHER_IN_STAGE = "other scripts in stage";

    CommonStmtConverter(ProgramParserState state) {
        super(state);
    }

    @Override
    CommonStmt convertStmt(final RawBlockId blockId, final RawBlock.RawRegularBlock block) {
        final CommonStmtOpcode opcode = CommonStmtOpcode.valueOf(block.opcode());
        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(blockId, block);

        return switch (opcode) {
            case control_wait -> {
                final NumExpr waitTime = NumExprConverter.convertNumExpr(state, block, KnownInputs.DURATION);
                yield new WaitSeconds(waitTime, metadata);
            }
            case control_wait_until -> convertWaitUntil(block, metadata);
            case control_stop -> convertStop(block, metadata);
            case control_create_clone_of -> convertCreateCloneOf(blockId, block);
            case event_broadcast -> {
                final StringExpr broadcast = StringExprConverter.convertStringExpr(
                        state, block, KnownInputs.BROADCAST_INPUT
                );
                final Message message = new Message(broadcast);

                yield new Broadcast(message, metadata);
            }
            case event_broadcastandwait -> {
                final StringExpr broadcast = StringExprConverter.convertStringExpr(
                        state, block, KnownInputs.BROADCAST_INPUT
                );
                final Message message = new Message(broadcast);

                yield new BroadcastAndWait(message, metadata);
            }
            case sensing_resettimer -> new ResetTimer(metadata);
            case data_changevariableby -> {
                final NumExpr by = NumExprConverter.convertNumExpr(state, block, KnownInputs.VALUE);
                final Qualified variable = getOrCreateReferencedVariable(block);

                yield new ChangeVariableBy(variable, by, metadata);
            }
        };
    }

    private WaitUntil convertWaitUntil(final RawBlock.RawRegularBlock block, final BlockMetadata metadata) {
        final Optional<RawInput> conditionInput = block.getOptionalInput(KnownInputs.CONDITION);
        final BoolExpr condition;

        if (conditionInput.isPresent()) {
            condition = BoolExprConverter.convertBoolExpr(state, block, conditionInput.get());
        } else {
            condition = new UnspecifiedBoolExpr();
        }

        return new WaitUntil(condition, metadata);
    }

    private CommonStmt convertStop(final RawBlock.RawRegularBlock block, final BlockMetadata metadata) {
        final String stopOptionValue = block.getFieldValueAsString(KnownFields.STOP_OPTION);

        return switch (stopOptionValue) {
            case STOP_OTHER, STOP_OTHER_IN_STAGE -> new StopOtherScriptsInSprite(metadata);
            default -> throw new InternalParsingException("Unknown stop option: " + stopOptionValue);
        };
    }

    private CreateCloneOf convertCreateCloneOf(final RawBlockId blockId, final RawBlock.RawRegularBlock block) {
        final RawInput cloneOptionInput = block.getInput(KnownInputs.CLONE_OPTION);

        if (
                ShadowType.SHADOW.equals(cloneOptionInput.shadowType())
                && cloneOptionInput.input() instanceof BlockRef.IdRef menuRef
        ) {
            return convertCreateCloneOfWithMenu(blockId, block, menuRef.id());
        } else {
            final StringExpr target = StringExprConverter.convertStringExpr(state, block, cloneOptionInput);
            final BlockMetadata blockMetadata = RawBlockMetadataConverter.convertBlockWithMenuMetadata(
                    blockId, block, new NoBlockMetadata()
            );

            return new CreateCloneOf(target, blockMetadata);
        }
    }

    private CreateCloneOf convertCreateCloneOfWithMenu(
            final RawBlockId blockId, final RawBlock.RawRegularBlock block, final RawBlockId menuRef
    ) {
        final RawBlock menu = state.getBlock(menuRef);
        if (!(menu instanceof RawBlock.RawRegularBlock menuBlock)) {
            throw new InternalParsingException("Unknown menu representation.");
        }

        final String targetName = menuBlock.getFieldValueAsString(KnownFields.CLONE_OPTION);
        final StrId target = new StrId(targetName);

        final BlockMetadata menuMetadata = RawBlockMetadataConverter.convertBlockMetadata(menuRef, menuBlock);
        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockWithMenuMetadata(
                blockId, block, menuMetadata
        );

        return new CreateCloneOf(new AsString(target), metadata);
    }
}
