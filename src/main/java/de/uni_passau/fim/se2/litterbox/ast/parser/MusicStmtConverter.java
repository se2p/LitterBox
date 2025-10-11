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

import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.drums.Drum;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.drums.ExprDrum;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.drums.FixedDrum;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.instruments.ExprInstrument;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.instruments.FixedInstrument;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.instruments.Instrument;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.notes.ExprNote;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.notes.FixedNote;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.notes.Note;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.DependentBlockOpcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.MusicOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast.*;

final class MusicStmtConverter extends StmtConverter<MusicStmt> {

    MusicStmtConverter(ProgramParserState state) {
        super(state);
    }

    @Override
    MusicStmt convertStmt(final RawBlockId blockId, final RawBlock.RawRegularBlock block) {
        final MusicOpcode opcode = MusicOpcode.valueOf(block.opcode());
        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(blockId, block);

        return switch (opcode) {
            case music_playDrumForBeats -> convertPlayDrumForBeats(block, metadata);
            case music_restForBeats -> {
                final NumExpr beats = NumExprConverter.convertNumExpr(state, block, KnownInputs.BEATS);
                yield new RestForBeats(beats, metadata);
            }
            case music_playNoteForBeats -> convertPlayNoteForBeats(block, metadata);
            case music_setInstrument -> convertSetInstrument(block, metadata);
            case music_setTempo -> {
                final NumExpr tempo = NumExprConverter.convertNumExpr(state, block, KnownInputs.TEMPO);
                yield new SetTempoTo(tempo, metadata);
            }
            case music_changeTempo -> {
                final NumExpr tempo = NumExprConverter.convertNumExpr(state, block, KnownInputs.TEMPO);
                yield new ChangeTempoBy(tempo, metadata);
            }
        };
    }

    private PlayDrumForBeats convertPlayDrumForBeats(
            final RawBlock.RawRegularBlock block, final BlockMetadata metadata
    ) {
        final RawInput drumInput = block.getInput(KnownInputs.DRUM);
        final Drum drum;

        if (ShadowType.SHADOW.equals(drumInput.shadowType())
                && drumInput.input() instanceof BlockRef.IdRef(RawBlockId menuId)
                && state.getBlock(menuId) instanceof RawBlock.RawRegularBlock menuBlock
                && DependentBlockOpcode.music_menu_DRUM.getName().equals(menuBlock.opcode())
        ) {
            final String drumName = menuBlock.getFieldValueAsString(KnownFields.DRUM);
            final BlockMetadata menuMeta = RawBlockMetadataConverter.convertBlockMetadata(menuId, menuBlock);
            drum = new FixedDrum(drumName, menuMeta);
        } else {
            final Expression expr = ExprConverter.convertExpr(state, block, drumInput);
            drum = new ExprDrum(expr, new NoBlockMetadata());
        }

        final NumExpr beats = NumExprConverter.convertNumExpr(state, block, KnownInputs.BEATS);

        return new PlayDrumForBeats(drum, beats, metadata);
    }

    private PlayNoteForBeats convertPlayNoteForBeats(
            final RawBlock.RawRegularBlock block, final BlockMetadata metadata
    ) {
        final RawInput noteInput = block.getInput(KnownInputs.NOTE);
        final Note note;

        if (ShadowType.SHADOW.equals(noteInput.shadowType())) {
            note = getNoteFromMenu(noteInput);
        } else {
            final Expression expr = ExprConverter.convertExpr(state, block, noteInput);
            note = new ExprNote(expr, new NoBlockMetadata());
        }

        final NumExpr beats = NumExprConverter.convertNumExpr(state, block, KnownInputs.BEATS);

        return new PlayNoteForBeats(note, beats, metadata);
    }

    private Note getNoteFromMenu(final RawInput noteInput) {
        if (noteInput.input() instanceof BlockRef.IdRef(RawBlockId menuId)
                && state.getBlock(menuId) instanceof RawBlock.RawRegularBlock menuBlock
                && DependentBlockOpcode.note.getName().equals(menuBlock.opcode())
        ) {
            final BlockMetadata menuMeta = RawBlockMetadataConverter.convertBlockMetadata(menuId, menuBlock);
            final double noteValue = getLiteralNoteValue(menuBlock);

            return new FixedNote(noteValue, menuMeta);
        } else if (noteInput.input() instanceof BlockRef.Block(RawBlock.ArrayBlock arrayBlock)) {
            if (arrayBlock instanceof RawBlock.RawIntBlockLiteral(long value)) {
                return new FixedNote(value, new NoBlockMetadata());
            } else if (arrayBlock instanceof RawBlock.RawFloatBlockLiteral(double value)) {
                return new FixedNote(value, new NoBlockMetadata());
            } else if (arrayBlock instanceof RawBlock.RawStringLiteral(String s)) {
                try {
                    final double value = Double.parseDouble(s);
                    return new FixedNote(value, new NoBlockMetadata());
                } catch (NumberFormatException e) {
                    throw new InternalParsingException("Unknown format for musical note in PlayNoteForBeats.", e);
                }
            }
        }

        throw new InternalParsingException("Unknown format for musical note in PlayNoteForBeats.");
    }

    private static double getLiteralNoteValue(final RawBlock.RawRegularBlock menuBlock) {
        final Object rawNoteValue = menuBlock.getField(KnownFields.NOTE).value();

        if (rawNoteValue instanceof Double d) {
            return d;
        } else if (rawNoteValue instanceof Integer i) {
            return i;
        }

        try {
            return Double.parseDouble(rawNoteValue.toString());
        } catch (NumberFormatException e) {
            throw new InternalParsingException("Musical note is not a number: " + rawNoteValue);
        }
    }

    private SetInstrumentTo convertSetInstrument(final RawBlock.RawRegularBlock block, final BlockMetadata metadata) {
        final RawInput instrumentInput = block.getInput(KnownInputs.INSTRUMENT);
        final Instrument instrument;

        if (ShadowType.SHADOW.equals(instrumentInput.shadowType())
                && instrumentInput.input() instanceof BlockRef.IdRef(RawBlockId menuId)
                && state.getBlock(menuId) instanceof RawBlock.RawRegularBlock menuBlock
                && DependentBlockOpcode.music_menu_INSTRUMENT.getName().equals(menuBlock.opcode())
        ) {
            final String instrumentName = menuBlock.getFieldValueAsString(KnownFields.INSTRUMENT);
            final BlockMetadata menuMeta = RawBlockMetadataConverter.convertBlockMetadata(menuId, menuBlock);
            instrument = new FixedInstrument(instrumentName, menuMeta);
        } else {
            final Expression expr = ExprConverter.convertExpr(state, block, instrumentInput);
            instrument = new ExprInstrument(expr, new NoBlockMetadata());
        }

        return new SetInstrumentTo(instrument, metadata);
    }
}
