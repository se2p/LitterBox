/*
 * Copyright (C) 2021-2024 EmbeddedKittens contributors
 *
 * This file is part of EmbeddedKittens.
 *
 * EmbeddedKittens is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * EmbeddedKittens is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EmbeddedKittens. If not, see <http://www.gnu.org/licenses/>.
 *
 * SPDX-FileCopyrightText: 2021-2024 EmbeddedKittens contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package de.uni_passau.fim.se2.embedded_kittens.tokenizer;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.uni_passau.fim.se2.embedded_kittens.MLOutputPath;
import de.uni_passau.fim.se2.embedded_kittens.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.embedded_kittens.shared.ActorNameNormalizer;
import de.uni_passau.fim.se2.embedded_kittens.util.AbstractTokenCheck;
import de.uni_passau.fim.se2.embedded_kittens.util.MaskingStrategy;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;

class TokenizerAbstractTokenTest extends AbstractTokenCheck {

    @Override
    protected Set<String> getSpecialAllowedTokens() {
        final Set<String> allowedTokens = new HashSet<>(
            Set.of("BEGIN", "END", "BEGIN_SCRIPT", "END_SCRIPT", "BEGIN_PROCEDURE", "END_PROCEDURE")
        );

        Arrays.stream(Token.values()).map(Token::getStrRep).forEach(allowedTokens::add);

        return allowedTokens;
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "src/test/fixtures/allBlocks.json",
            "src/test/fixtures/customBlocks.json",
            "src/test/fixtures/ml_preprocessing/astnn/custom_block.json",
            "src/test/fixtures/ml_preprocessing/astnn/messages.json",
            "src/test/fixtures/ml_preprocessing/shared/music_blocks.json",
            "src/test/fixtures/ml_preprocessing/shared/pen_blocks.json",
            "src/test/fixtures/ml_preprocessing/shared/tts_blocks.json",
        }
    )
    void testAllBlocksVisitableAbstract(final String filename) throws ParsingException {
        final TokenizingProgramPreprocessor analyzer = getAnalyzer();
        final File inputFile = Path.of(filename).toFile();
        final Program program = new Scratch3Parser().parseFile(inputFile);
        final Stream<TokenSequence> output = analyzer.processWholeProgram(program);

        output
            .map(TokenSequence::tokens)
            .flatMap(List::stream)
            .flatMap(List::stream)
            .forEach(this::checkNodeLabel);
    }

    private TokenizingProgramPreprocessor getAnalyzer() {
        final MLPreprocessorCommonOptions common = new MLPreprocessorCommonOptions(
            MLOutputPath.console(),
            true,
            true,
            true,
            ActorNameNormalizer.getDefault()
        );
        return new TokenizingProgramPreprocessor(
            common, MaskingStrategy.none(), false, TokenizingMethod.REGULAR, false
        );
    }
}
