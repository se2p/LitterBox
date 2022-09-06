/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.parser.stmt.mblock;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.SoundList;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.SoundNote;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.speaker.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.mblock.SpeakerStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.NumExprParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.BlockMetadataParser;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class SpeakerStmtParser {
    private static final String SOUNDLIST_KEY = "SOUNDLIST";
    private static final String SOUNDBEAT_KEY = "SOUNDBEAT";
    private static final String SOUNDNOTE_KEY = "SOUNDNOTE";
    private static final String HZ_KEY = "HZ";
    private static final String NOTE_KEY = "NOTE";
    private static final String BEAT_KEY = "BEAT";

    public static SpeakerStmt parse(final ProgramParserState state, String blockId, JsonNode current, JsonNode blocks) throws ParsingException {
        Preconditions.checkNotNull(blockId);
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(blocks);

        String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
                .checkArgument(SpeakerStmtOpcode.contains(opcodeString), "Given blockId does not point to an Speaker block.");

        SpeakerStmtOpcode opcode = SpeakerStmtOpcode.getOpcode(opcodeString);
        BlockMetadata metadata = BlockMetadataParser.parse(blockId, current);
        switch (opcode) {
            case show_play_sound:
                String fileName = current.get(FIELDS_KEY).get(SOUNDLIST_KEY).get(0).asText();
                SoundList soundList = new SoundList(fileName);
                return new PlaySound(soundList, metadata);

            case show_play_sound_wait:
                fileName = current.get(FIELDS_KEY).get(SOUNDLIST_KEY).get(0).asText();
                soundList = new SoundList(fileName);
                return new PlaySoundWait(soundList, metadata);

            case show_stop_allsound:
                return new StopAllSounds2(metadata);

            case show_play_note_with_string:
                String noteName = current.get(FIELDS_KEY).get(SOUNDNOTE_KEY).get(0).asText();
                SoundNote soundNote = new SoundNote(noteName);
                NumExpr beat = NumExprParser.parseNumExpr(state, current, SOUNDBEAT_KEY, blocks);
                return new PlayNote(soundNote, beat, metadata);

            case show_pause_note:
                beat = NumExprParser.parseNumExpr(state, current, TIME_KEY, blocks);
                return new Pause(beat, metadata);

            case show_play_hz:
            case sound_play_hz:
                NumExpr frequency = NumExprParser.parseNumExpr(state, current, HZ_KEY, blocks);
                NumExpr time = NumExprParser.parseNumExpr(state, current, TIME_KEY, blocks);
                return new PlayFrequency(frequency, time, metadata);

            case show_change_volume:
                NumExpr volume = NumExprParser.parseNumExpr(state, current, VOLUME_KEY_CAPS, blocks);
                return new ChangeVolumeBy2(volume, metadata);

            case show_set_volume:
                volume = NumExprParser.parseNumExpr(state, current, VOLUME_KEY_CAPS, blocks);
                return new SetVolumeTo2(volume, metadata);

            case sound_play_note:
                noteName = current.get(FIELDS_KEY).get(NOTE_KEY).get(0).asText();
                soundNote = new SoundNote(noteName);
                beat = NumExprParser.parseNumExpr(state, current, BEAT_KEY, blocks);
                return new PlayNote(soundNote, beat, metadata);

            default:
                throw new IllegalStateException("SpeakerStmt Block with opcode " + opcode + " was not parsed");
        }
    }
}
