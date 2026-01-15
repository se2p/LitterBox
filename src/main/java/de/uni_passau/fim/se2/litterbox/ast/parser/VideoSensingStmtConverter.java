/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.SetVideoTransparency;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.TurnVideo;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.DependentBlockOpcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.VideoSensingOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast.*;

final class VideoSensingStmtConverter extends StmtConverter<Stmt> {

    VideoSensingStmtConverter(final ProgramParserState state) {
        super(state);
    }

    @Override
    Stmt convertStmt(final RawBlockId blockId, final RawBlock.RawRegularBlock block) {
        final VideoSensingOpcode opcode = VideoSensingOpcode.valueOf(block.opcode());
        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(blockId, block);

        return switch (opcode) {
            case videoSensing_videoToggle -> {
                final String videoState = getVideoState(block);
                yield new TurnVideo(videoState, metadata);
            }
            case videoSensing_setVideoTransparency -> {
                final NumExpr transparency = NumExprConverter.convertNumExpr(state, block, KnownInputs.TRANSPARENCY);
                yield new SetVideoTransparency(transparency, metadata);
            }
            case videoSensing_videoOn -> {
                final NumExpr videoOn = NumExprConverter.convertNumExpr(state, blockId, block);
                yield new ExpressionStmt(videoOn);
            }
            default -> throw new InternalParsingException("Unknown Video Sensing opcode: " + opcode);
        };
    }

    /**
     * Extracts the VIDEO_STATE value from either a field or an input with shadow block.
     *
     * <p>Scratch projects can have VIDEO_STATE in two formats:
     * <ul>
     *   <li>Field-based (older/simple): {@code "fields": {"VIDEO_STATE": ["on", null]}}</li>
     *   <li>Input-based (newer/complex): {@code "inputs": {"VIDEO_STATE": [1, "shadowBlockId"]}}
     *       referencing a videoSensing_menu_VIDEO_STATE shadow block</li>
     * </ul>
     *
     * @param block The videoSensing_videoToggle block
     * @return The video state value ("on", "off", or "on-flipped")
     */
    private String getVideoState(final RawBlock.RawRegularBlock block) {
        // First, try the field-based format (older projects)
        if (block.hasField(KnownFields.VIDEO_STATE)) {
            return block.getFieldValueAsString(KnownFields.VIDEO_STATE);
        }

        // Then, try the input-based format with shadow block (newer projects)
        if (block.hasInput(KnownInputs.VIDEO_STATE)) {
            final RawInput videoStateInput = block.getInput(KnownInputs.VIDEO_STATE);
            if (ShadowType.SHADOW.equals(videoStateInput.shadowType())
                    && videoStateInput.input() instanceof BlockRef.IdRef(RawBlockId menuId)
                    && state.getBlock(menuId) instanceof RawBlock.RawRegularBlock menuBlock
                    && DependentBlockOpcode.videoSensing_menu_VIDEO_STATE.getName().equals(menuBlock.opcode())) {
                return menuBlock.getFieldValueAsString(KnownFields.VIDEO_STATE);
            }
        }

        // Fallback for malformed blocks - default to "off"
        return "off";
    }
}
