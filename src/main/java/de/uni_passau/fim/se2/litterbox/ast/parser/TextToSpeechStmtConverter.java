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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.SetLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.SetVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.Speak;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.TextToSpeechStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.ExprLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.FixedLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.Language;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.ExprVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.FixedVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.Voice;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.DependentBlockOpcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.TextToSpeechOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast.*;

final class TextToSpeechStmtConverter extends StmtConverter<TextToSpeechStmt> {

    TextToSpeechStmtConverter(ProgramParserState state) {
        super(state);
    }

    @Override
    TextToSpeechStmt convertStmt(final RawBlockId blockId, final RawBlock.RawRegularBlock block) {
        final TextToSpeechOpcode opcode = TextToSpeechOpcode.valueOf(block.opcode());
        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(blockId, block);

        return switch (opcode) {
            case text2speech_speakAndWait -> {
                final StringExpr text = StringExprConverter.convertStringExpr(state, block, KnownInputs.WORDS);
                yield new Speak(text, metadata);
            }
            case text2speech_setVoice -> convertSetVoice(block, metadata);
            case text2speech_setLanguage -> convertSetLanguage(block, metadata);
        };
    }

    private SetLanguage convertSetLanguage(final RawBlock.RawRegularBlock block, final BlockMetadata metadata) {
        final RawInput languageInput = block.getInput(KnownInputs.LANGUAGE);
        final Language language;

        if (ShadowType.SHADOW.equals(languageInput.shadowType())
                && languageInput.input() instanceof BlockRef.IdRef(RawBlockId menuId)
                && state.getBlock(menuId) instanceof RawBlock.RawRegularBlock menuBlock
                && DependentBlockOpcode.text2speech_menu_languages.getName().equals(menuBlock.opcode())
        ) {
            final String languageName = menuBlock.getFieldValueAsString(KnownFields.LANGUAGES);
            final BlockMetadata menuMeta = RawBlockMetadataConverter.convertBlockMetadata(menuId, menuBlock);
            language = new FixedLanguage(languageName, menuMeta);
        } else {
            final Expression expr = ExprConverter.convertExpr(state, block, languageInput);
            language = new ExprLanguage(expr, new NoBlockMetadata());
        }

        return new SetLanguage(language, metadata);
    }

    private SetVoice convertSetVoice(final RawBlock.RawRegularBlock block, final BlockMetadata metadata) {
        final RawInput voiceInput = block.getInput(KnownInputs.VOICE);
        final Voice voice;

        if (ShadowType.SHADOW.equals(voiceInput.shadowType())
                && voiceInput.input() instanceof BlockRef.IdRef(RawBlockId menuId)
                && state.getBlock(menuId) instanceof RawBlock.RawRegularBlock menuBlock
                && DependentBlockOpcode.text2speech_menu_voices.getName().equals(menuBlock.opcode())
        ) {
            final String voiceName = menuBlock.getFieldValueAsString(KnownFields.VOICES);
            final BlockMetadata menuMeta = RawBlockMetadataConverter.convertBlockMetadata(menuId, menuBlock);
            voice = new FixedVoice(voiceName, menuMeta);
        } else {
            final Expression expr = ExprConverter.convertExpr(state, block, voiceInput);
            voice = new ExprVoice(expr, new NoBlockMetadata());
        }

        return new SetVoice(voice, metadata);
    }
}
