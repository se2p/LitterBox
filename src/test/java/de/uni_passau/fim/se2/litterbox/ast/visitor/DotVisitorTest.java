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
package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;

class DotVisitorTest {
    @ParameterizedTest
    @MethodSource("getStringLiterals")
    void testStringLiteral(final String input, final String expectedLabel) {
        final StringLiteral s = new StringLiteral(input);
        final String graph = DotVisitor.buildDotGraph(s);

        assertThat(graph).contains(String.format("[label = \"%s\"];", expectedLabel));
    }

    private static Stream<Arguments> getStringLiterals() {
        return Stream.of(
                Arguments.of("", ""),
                Arguments.of("simple string", "simple string"),
                Arguments.of("string with \" in it", "string with \\\" in it"),
                Arguments.of("backslash \\ escaped", "backslash \\\\ escaped"),
                Arguments.of("backslash \\\" escaped", "backslash \\\\\\\" escaped")
        );
    }

    @Test
    void testNumberLiteral() {
        final NumberLiteral n = new NumberLiteral(12.2);
        final String graph = DotVisitor.buildDotGraph(n);

        assertThat(graph).contains("[label = \"12.2\"];");
    }

    @Test
    void testBoolLiteral() {
        final BoolLiteral t = new BoolLiteral(true);
        assertThat(DotVisitor.buildDotGraph(t)).contains("[label = \"true\"];");

        final BoolLiteral f = new BoolLiteral(false);
        assertThat(DotVisitor.buildDotGraph(f)).contains("[label = \"false\"];");
    }

    @Test
    void testColorLiteral() {
        final ColorLiteral c = new ColorLiteral(12, 28, 255);
        final String graph = DotVisitor.buildDotGraph(c);

        assertThat(graph).contains("[label = \"#0c1cff\"];");
    }
}
