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

import de.uni_passau.fim.se2.litterbox.ast.model.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.event.BoardLaunch;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.event.BoardShaken;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawBlock;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawBlockId;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawTarget;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.EventOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;

class EventConverter {
    private EventConverter() {
        throw new IllegalCallerException("utility class constructor");
    }

    static Event convertEvent(
            final ProgramParserState state,
            final RawTarget target,
            final RawBlockId id,
            final RawBlock.RawRegularBlock event
    ) {
        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(id, event);
        final EventOpcode opcode = EventOpcode.getOpcode(event.opcode());

        return switch (opcode) {
            case event_whenflagclicked -> new GreenFlag(metadata);
            case event_whenstageclicked -> new StageClicked(metadata);
            case event_whenthisspriteclicked -> new SpriteClicked(metadata);
            case control_start_as_clone -> new StartedAsClone(metadata);
            case when_board_launch, main -> new BoardLaunch(metadata);
            case when_board_shake -> new BoardShaken(metadata);
            case event_whenkeypressed -> null;
            case event_whenbroadcastreceived -> null;
            case event_whenbackdropswitchesto -> null;
            case event_whengreaterthan -> null;
            case when_button_press -> null;
            case when_board_button -> null;
            case when_board_tilt -> null;
            case when_volume_over -> null;
            case when_brightness_less -> null;
        };
    }
}
