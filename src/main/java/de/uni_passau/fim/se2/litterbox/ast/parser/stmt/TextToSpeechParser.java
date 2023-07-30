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
package de.uni_passau.fim.se2.litterbox.ast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.SetLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.SetVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.Speak;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.ExprLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.FixedLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.Language;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.ExprVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.FixedVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.Voice;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.DependentBlockOpcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.TextToSpeechOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.ExpressionParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;
import de.uni_passau.fim.se2.litterbox.ast.parser.StringExprParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.BlockMetadataParser;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class TextToSpeechParser {

    public static Stmt parse(final ProgramParserState state, String blockId, JsonNode current, JsonNode blocks)
            throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(blocks);
        final String opCodeString = current.get(Constants.OPCODE_KEY).asText();
        if (!TextToSpeechOpcode.contains(opCodeString)) {
            throw new ParsingException(
                    "Called parse TextToSpeech with a block that does not qualify as such"
                            + " a statement. Opcode is " + opCodeString);
        }
        final TextToSpeechOpcode opcode = TextToSpeechOpcode.valueOf(opCodeString);
        BlockMetadata metadata = BlockMetadataParser.parse(blockId, current);
        return switch (opcode) {
            case text2speech_setVoice -> parseSetVoice(state, current, metadata, blocks);
            case text2speech_speakAndWait -> {
                StringExpr expr = StringExprParser.parseStringExpr(state, current, WORDS_KEY, blocks);
                yield new Speak(expr, metadata);
            }
            case text2speech_setLanguage -> parseSetLanguage(state, current, metadata, blocks);
        };
    }

    private static Stmt parseSetLanguage(final ProgramParserState state, JsonNode current, BlockMetadata metadata,
                                         JsonNode blocks) throws ParsingException {
        Language lang;
        BlockMetadata paramMetadata;
        List<JsonNode> inputsList = new ArrayList<>();
        current.get(Constants.INPUTS_KEY).elements().forEachRemaining(inputsList::add);

        if (getShadowIndicator((ArrayNode) inputsList.get(0)) == 1) {
            String reference = current.get(INPUTS_KEY).get(LANGUAGE_INPUT_KEY).get(POS_INPUT_VALUE).asText();
            JsonNode referredBlock = blocks.get(reference);
            Preconditions.checkNotNull(referredBlock);

            if (referredBlock.get(OPCODE_KEY).asText().equals(DependentBlockOpcode.text2speech_menu_languages.name())) {
                JsonNode languageParamNode = referredBlock.get(FIELDS_KEY).get(LANGUAGE_FIELDS_KEY);
                Preconditions.checkArgument(languageParamNode.isArray());
                String attribute = languageParamNode.get(FIELD_VALUE).asText();
                paramMetadata = BlockMetadataParser.parse(reference, referredBlock);
                lang = new FixedLanguage(attribute, paramMetadata);
            } else {
                paramMetadata = new NoBlockMetadata();
                Expression expr = ExpressionParser.parseExpr(state, current, LANGUAGE_INPUT_KEY, blocks);
                lang = new ExprLanguage(expr, paramMetadata);
            }
        } else {
            paramMetadata = new NoBlockMetadata();
            Expression expr = ExpressionParser.parseExpr(state, current, LANGUAGE_INPUT_KEY, blocks);
            lang = new ExprLanguage(expr, paramMetadata);
        }

        return new SetLanguage(lang, metadata);
    }

    private static Stmt parseSetVoice(final ProgramParserState state, JsonNode current, BlockMetadata metadata,
                                      JsonNode blocks) throws ParsingException {
        Voice voice;
        BlockMetadata paramMetadata;
        List<JsonNode> inputsList = new ArrayList<>();
        current.get(Constants.INPUTS_KEY).elements().forEachRemaining(inputsList::add);

        if (getShadowIndicator((ArrayNode) inputsList.get(0)) == 1) {
            String reference = current.get(INPUTS_KEY).get(VOICE_INPUT_KEY).get(POS_INPUT_VALUE).asText();
            JsonNode referredBlock = blocks.get(reference);
            Preconditions.checkNotNull(referredBlock);

            if (referredBlock.get(OPCODE_KEY).asText().equals(DependentBlockOpcode.text2speech_menu_voices.name())) {
                JsonNode voiceParamNode = referredBlock.get(FIELDS_KEY).get(VOICE_FIELDS_KEY);
                Preconditions.checkArgument(voiceParamNode.isArray());
                String attribute = voiceParamNode.get(FIELD_VALUE).asText();
                paramMetadata = BlockMetadataParser.parse(reference, referredBlock);
                voice = new FixedVoice(attribute, paramMetadata);
            } else {
                paramMetadata = new NoBlockMetadata();
                Expression expr = ExpressionParser.parseExpr(state, current, VOICE_INPUT_KEY, blocks);
                voice = new ExprVoice(expr, paramMetadata);
            }
        } else {
            paramMetadata = new NoBlockMetadata();
            Expression expr = ExpressionParser.parseExpr(state, current, VOICE_INPUT_KEY, blocks);
            voice = new ExprVoice(expr, paramMetadata);
        }

        return new SetVoice(voice, metadata);
    }

    static int getShadowIndicator(ArrayNode exprArray) {
        return exprArray.get(Constants.POS_INPUT_SHADOW).asInt();
    }
}
