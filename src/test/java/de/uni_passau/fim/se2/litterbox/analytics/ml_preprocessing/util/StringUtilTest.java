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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util;

import de.uni_passau.fim.se2.litterbox.utils.Pair;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class StringUtilTest {

    @Test
    void testNormalizeString() {
        final List<Pair<String>> beforeAfterPairs = List.of(
                Pair.of("a_b", "a  \tb"),
                Pair.of("a_b", "\na\nb\n"),
                Pair.of("1_a_456_b_68", "1a456b68"),
                Pair.of("1_a_456_b_68", "1A456B68"),
                Pair.of("1_a_4_56_b_68", "1{a4]56B/68"),
                Pair.of("äa_b", "{}äa'b\n"),
                Pair.of("pinos_de_boliche_1975_89_removebg_preview_3", "pinos-de-boliche_1975-89-removebg-preview3"),
                Pair.of("download_48", "download (48)")
        );

        assertAll(
                beforeAfterPairs
                        .stream()
                        .map(pair -> () -> assertEquals(pair.getFst(), StringUtil.normaliseString(pair.getSnd())))
        );
    }

    @Test
    void testNormalizeStringWithDelimiter() {
        final List<Pair<String>> beforeAfterPairs = List.of(
                Pair.of("a|b", "a  \tb"),
                Pair.of("a|b", "\na\nb\n"),
                Pair.of("1|a|456|b|68", "1a456b68"),
                Pair.of("1|a|456|b|68", "1A456B68"),
                Pair.of("1|a|4|56|b|68", "1{a4]56B/68"),
                Pair.of("äa|b", "{}äa'b\n")
        );

        assertAll(
                beforeAfterPairs
                        .stream()
                        .map(pair -> () -> assertEquals(pair.getFst(),
                                StringUtil.normaliseString(pair.getSnd(), "|")))
        );
    }

    @Test
    void testSplitToSubtokensSimple() {
        List<String> subTokensTest1 = StringUtil.splitToSubtokens("ForeverAlone");
        assertThat(subTokensTest1).containsExactly("Forever", "Alone");
    }

    @Test
    void testSplitToSubtokens() {
        List<String> subTokensTest2 = StringUtil.splitToSubtokens("Forever   _ Alone-3\ninThis98World?! ");
        assertThat(subTokensTest2).containsExactly("Forever", "Alone", "3", "in", "This", "98", "World", "?!");
    }

    @Test
    void testKeepSomePunctuation() {
        final List<String> subtokens = StringUtil.splitToNormalisedSubtokens("abc!-def", "_");
        assertThat(subtokens).containsExactly("abc", "!", "def");
    }

    @Test
    void regressionTestSpecialSpaces() {
        final String input = "day! Callooh! Callay!";
        final List<String> subtokens = StringUtil.splitToNormalisedSubtokens(input, "_");

        assertThat(subtokens).containsExactly("day", "!", "callooh", "!", "callay", "!");
    }
}
