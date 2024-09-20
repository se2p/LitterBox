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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.SoundList;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.SoundNote;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.speaker.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawBlock;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawBlockId;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.mblock.SpeakerStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;

final class SpeakerStmtConverter extends StmtConverter<SpeakerStmt> {

    private static final String SOUNDLIST_KEY = "SOUNDLIST";
    private static final String SOUNDBEAT_KEY = "SOUNDBEAT";
    private static final String SOUNDNOTE_KEY = "SOUNDNOTE";
    private static final String HZ_KEY = "HZ";
    private static final String NOTE_KEY = "NOTE";
    private static final String BEAT_KEY = "BEAT";

    SpeakerStmtConverter(final ProgramParserState state) {
        super(state);
    }

    @Override
    SpeakerStmt convertStmt(final RawBlockId blockId, final RawBlock.RawRegularBlock block) {
        final SpeakerStmtOpcode opcode = SpeakerStmtOpcode.getOpcode(block.opcode());
        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(blockId, block);

        return switch (opcode) {
            case show_stop_allsound -> new StopAllSounds2(metadata);
            case show_play_note_with_string -> getPlayNote(block, metadata, SOUNDNOTE_KEY, SOUNDBEAT_KEY);
            case sound_play_note -> getPlayNote(block, metadata, NOTE_KEY, BEAT_KEY);
            case show_play_sound -> {
                final String name = block.fields().get(SOUNDLIST_KEY).value().toString();
                final SoundList soundList = new SoundList(name);
                yield new PlaySound(soundList, metadata);
            }
            case show_play_sound_wait -> {
                final String name = block.fields().get(SOUNDLIST_KEY).value().toString();
                final SoundList soundList = new SoundList(name);
                yield new PlaySoundWait(soundList, metadata);
            }
            case show_pause_note -> {
                final NumExpr time = NumExprConverter.convertNumExpr(
                        state, block, block.inputs().get(Constants.TIME_KEY)
                );
                yield new Pause(time, metadata);
            }
            case show_play_hz, sound_play_hz -> {
                final NumExpr frequency = NumExprConverter.convertNumExpr(state, block, block.inputs().get(HZ_KEY));
                final NumExpr time = NumExprConverter.convertNumExpr(
                        state, block, block.inputs().get(Constants.TIME_KEY)
                );
                yield new PlayFrequency(frequency, time, metadata);
            }
            case show_change_volume -> {
                final NumExpr volume = NumExprConverter.convertNumExpr(
                        state, block, block.inputs().get(Constants.VOLUME_KEY_CAPS)
                );
                yield new ChangeVolumeBy2(volume, metadata);
            }
            case show_set_volume -> {
                final NumExpr volume = NumExprConverter.convertNumExpr(
                        state, block, block.inputs().get(Constants.VOLUME_KEY_CAPS)
                );
                yield new SetVolumeTo2(volume, metadata);
            }
        };
    }

    private PlayNote getPlayNote(
            final RawBlock.RawRegularBlock block,
            final BlockMetadata metadata,
            final String noteKey,
            final String beatKey
    ) {
        final String noteName = block.fields().get(noteKey).value().toString();
        final SoundNote note = new SoundNote(noteName);
        final NumExpr beat = NumExprConverter.convertNumExpr(state, block, block.inputs().get(beatKey));

        return new PlayNote(note, beat, metadata);
    }
}
