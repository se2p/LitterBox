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

import static org.junit.jupiter.api.Assertions.*;

class StringUtilTest {

    @Test
    void testNormalizeString() {
        final List<Pair<String>> beforeAfterPairs = List.of(
                Pair.of("a_b", "a  \tb"),
                Pair.of("a_b", "\na\nb\n"),
                Pair.of("1a456b68", "1a456b68"),
                Pair.of("ab", "{}äa'b\n")
        );

        assertAll(
                beforeAfterPairs
                        .stream()
                        .map(pair -> () -> assertEquals(pair.getFst(), StringUtil.normaliseString(pair.getSnd())))
        );
    }

    @Test
    void testSplitToSubtokens() {
        List<String> subTokensTest1 = StringUtil.splitToSubtokens("ForeverAlone");
        assertEquals(2, subTokensTest1.size());
        assertEquals("forever", subTokensTest1.get(0));
        assertEquals("alone", subTokensTest1.get(1));
        List<String> subTokensTest2 = StringUtil.splitToSubtokens("Forever   _ Alone-3\ninThis98World?! ");
        assertEquals(5, subTokensTest2.size());
        assertEquals("forever", subTokensTest2.get(0));
        assertEquals("alone", subTokensTest2.get(1));
        assertEquals("in", subTokensTest2.get(2));
        assertEquals("this", subTokensTest2.get(3));
        assertEquals("world", subTokensTest2.get(4));
    }
}
