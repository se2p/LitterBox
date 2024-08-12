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
package de.uni_passau.fim.se2.litterbox.ast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
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
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.DependentBlockOpcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.MusicOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.ExpressionParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.NumExprParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.BlockMetadataParser;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class MusicStmtParser {

    public static Stmt parse(final ProgramParserState state, String blockId, JsonNode current, JsonNode blocks)
            throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(blocks);
        final String opCodeString = current.get(Constants.OPCODE_KEY).asText();
        if (!MusicOpcode.contains(opCodeString)) {
            throw new ParsingException(
                    "Called parse Music with a block that does not qualify as such"
                            + " a statement. Opcode is " + opCodeString);
        }
        final MusicOpcode opcode = MusicOpcode.valueOf(opCodeString);
        BlockMetadata metadata = BlockMetadataParser.parse(blockId, current);
        switch (opcode) {

            case music_playDrumForBeats:
                return parseDrumForBeats(state, current, metadata, blocks);
            case music_restForBeats:
                NumExpr expr = NumExprParser.parseNumExpr(state, current, BEATS_KEY, blocks);
                return new RestForBeats(expr, metadata);
            case music_playNoteForBeats:
                return parseNoteForBeats(state, current, metadata, blocks);
            case music_setInstrument:
                return parseSetInstrument(state, current, metadata, blocks);
            case music_setTempo:
                expr = NumExprParser.parseNumExpr(state, current, TEMPO_BIG_KEY, blocks);
                return new SetTempoTo(expr, metadata);
            case music_changeTempo:
                expr = NumExprParser.parseNumExpr(state, current, TEMPO_BIG_KEY, blocks);
                return new ChangeTempoBy(expr, metadata);
            default:
                throw new ParsingException("Not implemented yet for opcode " + opcode);
        }
    }

    private static Stmt parseSetInstrument(final ProgramParserState state, JsonNode current, BlockMetadata metadata,
                                           JsonNode blocks) throws ParsingException {
        Instrument instrument;
        BlockMetadata paramMetadata;
        List<JsonNode> inputsList = new ArrayList<>();
        current.get(Constants.INPUTS_KEY).elements().forEachRemaining(inputsList::add);

        if (getShadowIndicator((ArrayNode) inputsList.get(0)) == 1) {
            String reference = current.get(INPUTS_KEY).get(INSTRUMENT_KEY).get(POS_INPUT_VALUE).asText();
            JsonNode referredBlock = blocks.get(reference);
            Preconditions.checkNotNull(referredBlock);

            if (referredBlock.get(OPCODE_KEY).asText().equals(DependentBlockOpcode.music_menu_INSTRUMENT.name())) {
                JsonNode languageParamNode = referredBlock.get(FIELDS_KEY).get(INSTRUMENT_KEY);
                Preconditions.checkArgument(languageParamNode.isArray());
                String attribute = languageParamNode.get(FIELD_VALUE).asText();
                paramMetadata = BlockMetadataParser.parse(reference, referredBlock);
                instrument = new FixedInstrument(attribute, paramMetadata);
            } else {
                paramMetadata = new NoBlockMetadata();
                Expression expr = ExpressionParser.parseExpr(state, current, INSTRUMENT_KEY, blocks);
                instrument = new ExprInstrument(expr, paramMetadata);
            }
        } else {
            paramMetadata = new NoBlockMetadata();
            Expression expr = ExpressionParser.parseExpr(state, current, INSTRUMENT_KEY, blocks);
            instrument = new ExprInstrument(expr, paramMetadata);
        }

        return new SetInstrumentTo(instrument, metadata);
    }

    private static Stmt parseNoteForBeats(final ProgramParserState state, JsonNode current, BlockMetadata metadata,
                                          JsonNode blocks) throws ParsingException {
        Note note;
        BlockMetadata paramMetadata;
        List<JsonNode> inputsList = new ArrayList<>();
        current.get(Constants.INPUTS_KEY).elements().forEachRemaining(inputsList::add);

        if (getShadowIndicator((ArrayNode) inputsList.get(0)) == 1) {

            //It seems sometimes notes can be direct number values which will be parsed to literal to use in a note
            if (current.get(INPUTS_KEY).get(NOTE_KEY).get(POS_INPUT_VALUE).isArray()) {
                NumExpr numExpr = NumExprParser.parseNumExpr(state, current, NOTE_KEY, blocks);
                if (numExpr instanceof NumberLiteral literal) {
                    note = new FixedNote(literal.getValue(), new NoBlockMetadata());
                } else {
                    throw new ParsingException("A literal was found instead of a note and it was not a number value.");
                }
            } else {
                String reference = current.get(INPUTS_KEY).get(NOTE_KEY).get(POS_INPUT_VALUE).asText();
                JsonNode referredBlock = blocks.get(reference);
                Preconditions.checkNotNull(referredBlock);

                if (referredBlock.get(OPCODE_KEY).asText().equals(DependentBlockOpcode.note.name())) {
                    JsonNode languageParamNode = referredBlock.get(FIELDS_KEY).get(NOTE_KEY);
                    Preconditions.checkArgument(languageParamNode.isArray());
                    double attribute = languageParamNode.get(FIELD_VALUE).asDouble();
                    paramMetadata = BlockMetadataParser.parse(reference, referredBlock);
                    note = new FixedNote(attribute, paramMetadata);
                } else {
                    paramMetadata = new NoBlockMetadata();
                    Expression expr = ExpressionParser.parseExpr(state, current, NOTE_KEY, blocks);
                    note = new ExprNote(expr, paramMetadata);
                }
            }
        } else {
            paramMetadata = new NoBlockMetadata();
            Expression expr = ExpressionParser.parseExpr(state, current, NOTE_KEY, blocks);
            note = new ExprNote(expr, paramMetadata);
        }

        NumExpr expr = NumExprParser.parseNumExpr(state, current, BEATS_KEY, blocks);

        return new PlayNoteForBeats(note, expr, metadata);
    }

    private static Stmt parseDrumForBeats(final ProgramParserState state, JsonNode current, BlockMetadata metadata,
                                          JsonNode blocks) throws ParsingException {
        Drum drum;
        BlockMetadata paramMetadata;
        List<JsonNode> inputsList = new ArrayList<>();
        current.get(Constants.INPUTS_KEY).elements().forEachRemaining(inputsList::add);

        if (getShadowIndicator((ArrayNode) inputsList.get(0)) == 1) {
            String reference = current.get(INPUTS_KEY).get(DRUM_KEY).get(POS_INPUT_VALUE).asText();
            JsonNode referredBlock = blocks.get(reference);
            Preconditions.checkNotNull(referredBlock);

            if (referredBlock.get(OPCODE_KEY).asText().equals(DependentBlockOpcode.music_menu_DRUM.name())) {
                JsonNode languageParamNode = referredBlock.get(FIELDS_KEY).get(DRUM_KEY);
                Preconditions.checkArgument(languageParamNode.isArray());
                String attribute = languageParamNode.get(FIELD_VALUE).asText();
                paramMetadata = BlockMetadataParser.parse(reference, referredBlock);
                drum = new FixedDrum(attribute, paramMetadata);
            } else {
                paramMetadata = new NoBlockMetadata();
                Expression expr = ExpressionParser.parseExpr(state, current, DRUM_KEY, blocks);
                drum = new ExprDrum(expr, paramMetadata);
            }
        } else {
            paramMetadata = new NoBlockMetadata();
            Expression expr = ExpressionParser.parseExpr(state, current, DRUM_KEY, blocks);
            drum = new ExprDrum(expr, paramMetadata);
        }

        NumExpr expr = NumExprParser.parseNumExpr(state, current, BEATS_KEY, blocks);

        return new PlayDrumForBeats(drum, expr, metadata);
    }

    static int getShadowIndicator(ArrayNode exprArray) {
        return exprArray.get(Constants.POS_INPUT_SHADOW).asInt();
    }
}
