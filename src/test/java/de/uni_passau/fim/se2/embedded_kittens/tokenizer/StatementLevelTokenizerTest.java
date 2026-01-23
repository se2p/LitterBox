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

import static com.google.common.truth.Truth.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.uni_passau.fim.se2.embedded_kittens.JsonTest;
import de.uni_passau.fim.se2.embedded_kittens.util.MaskingStrategy;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

class StatementLevelTokenizerTest implements JsonTest {

    @Test
    void testTokenizeSimpleExpressionIgnored() throws ParsingException {
        final Program program = getAST("src/test/fixtures/ml_preprocessing/tokenizer/simple_binary_expr.json");
        final List<String> tokens = StatementLevelTokenizer.tokenize(program, program, false, MaskingStrategy.none());

        assertThat(tokens).containsExactly(
            "BEGIN_SPRITE", "BEGIN_SCRIPT", "event_never", "control_wait", "END_SCRIPT", "END_SPRITE"
        );
    }

    @Test
    void testMaskRegularStatement() throws ParsingException {
        final Program program = getAST("src/test/fixtures/ml_preprocessing/tokenizer/simple_binary_expr.json");
        final List<String> tokens = StatementLevelTokenizer
            .tokenize(program, program, false, MaskingStrategy.block("s}Q,l-$=;zl*WHEIQ-#r"));

        assertThat(tokens).containsExactly(
            "BEGIN_SPRITE", "BEGIN_SCRIPT", "event_never", "[MASK]", "END_SCRIPT", "END_SPRITE"
        );
    }

    @Test
    void testTokenizeIfElse() throws ParsingException {
        final Program program = getAST("src/test/fixtures/simpleIfElse.json");
        final List<String> tokens = StatementLevelTokenizer.tokenize(program, program, false, MaskingStrategy.none());

        assertThat(tokens).containsExactly(
            "BEGIN_SPRITE", "BEGIN_SCRIPT", "event_whenflagclicked", "control_if_else",
            "BEGIN_SUBSTACK", "looks_cleargraphiceffects", "END_SUBSTACK",
            "ELSE", "BEGIN_SUBSTACK", "sound_cleareffects", "END_SUBSTACK",
            "END_SCRIPT", "END_SPRITE"
        );
    }

    @Test
    void testTokenizeIfElseMasked() throws ParsingException {
        final Program program = getAST("src/test/fixtures/simpleIfElse.json");
        final List<String> tokens = StatementLevelTokenizer
            .tokenize(program, program, false, MaskingStrategy.block("v]7T@CzT2y51/G*z53r8"));

        assertThat(tokens).containsExactly(
            "BEGIN_SPRITE", "BEGIN_SCRIPT", "event_whenflagclicked", "[MASK]", "END_SCRIPT", "END_SPRITE"
        );
    }

    @Test
    void testTokenizeRepeat() throws ParsingException {
        final Program program = getAST("src/test/fixtures/simpleRepeatUntil.json");
        final List<String> tokens = StatementLevelTokenizer.tokenize(program, program, false, MaskingStrategy.none());

        assertThat(tokens).containsExactly(
            "BEGIN_SPRITE", "BEGIN_SCRIPT", "event_whenflagclicked", "control_repeat_until",
            "BEGIN_SUBSTACK", "looks_cleargraphiceffects", "END_SUBSTACK",
            "sound_cleareffects",
            "END_SCRIPT", "END_SPRITE"
        );
    }

    @Test
    void testTokenizeRepeatMasked() throws ParsingException {
        final Program program = getAST("src/test/fixtures/simpleRepeatUntil.json");
        final List<String> tokens = StatementLevelTokenizer
            .tokenize(program, program, false, MaskingStrategy.block("?~*3Bq5_ct;#4Bmncl%y"));

        assertThat(tokens).containsExactly(
            "BEGIN_SPRITE", "BEGIN_SCRIPT", "event_whenflagclicked", "[MASK]", "sound_cleareffects", "END_SCRIPT",
            "END_SPRITE"
        );
    }
}
