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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.shared;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.RotationStyle;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.google.common.truth.Truth.assertThat;

class TokenVisitorTest implements JsonTest {

    @Test
    void testIntegerNumber() {
        NumberLiteral literal = new NumberLiteral(1234);
        assertThat(TokenVisitor.getToken(literal)).isEqualTo("1234");
    }

    @Test
    void testIntegerRounded() {
        NumberLiteral literal = new NumberLiteral(1234.0);
        assertThat(TokenVisitor.getToken(literal)).isEqualTo("1234");
    }

    @Test
    void testDoubleNumber() {
        NumberLiteral literal = new NumberLiteral(3.14);
        assertThat(TokenVisitor.getToken(literal)).isEqualTo("3.14");
    }

    @Test
    void testDoubleNumberRounded() {
        NumberLiteral literal = new NumberLiteral(3.1415);
        assertThat(TokenVisitor.getToken(literal)).isEqualTo("3.14");
    }

    @Test
    void testStringWithWhitespace() {
        StringLiteral literal = new StringLiteral("hello\t world \n");
        assertThat(TokenVisitor.getToken(literal)).isEqualTo("hello\t world \n");
        assertThat(TokenVisitor.getNormalisedToken(literal)).isEqualTo("hello_world");
    }

    @Test
    void testStringWithComma() {
        StringLiteral literal = new StringLiteral("a,b,c");
        assertThat(TokenVisitor.getToken(literal)).isEqualTo("a,b,c");
        assertThat(TokenVisitor.getNormalisedToken(literal)).isEqualTo("a_b_c");
    }

    @Test
    void testStringIdWithComma() {
        StrId id = new StrId("a,b,c");
        assertThat(TokenVisitor.getNormalisedToken(id)).isEqualTo("a_b_c");
    }

    @Test
    void testKeepUnderscores() {
        final String s = "a_b-c/d";
        final StrId id = new StrId(s);
        assertThat(TokenVisitor.getToken(id)).isEqualTo(s);
        assertThat(TokenVisitor.getNormalisedToken(id)).isEqualTo("a_b_c_d");
    }

    @ParameterizedTest
    @ValueSource(strings = {"_", "__", "___"})
    void testRemoveSequentialUnderscores(final String s) {
        final StringLiteral literal = new StringLiteral("abc" + s + "def");
        assertThat(TokenVisitor.getNormalisedToken(literal)).isEqualTo("abc_def");
    }

    @ParameterizedTest
    @ValueSource(strings = {"_--.", "__", "_-__-", "{_},)-_+"})
    void testReplaceMultiplePunctuation(final String s) {
        final StrId id = new StrId("ab" + s + "def[]");
        assertThat(TokenVisitor.getNormalisedToken(id)).isEqualTo("ab_def");
    }

    @Test
    void testGreekString() {
        final String s = "ελληνικά";
        final StringLiteral literal = new StringLiteral(s);
        assertThat(TokenVisitor.getToken(literal)).isEqualTo(s);
        assertThat(TokenVisitor.getNormalisedToken(literal)).isEqualTo(s);
    }

    @Test
    void testGreekCamelCaseSplitting() {
        final String s = "ελληΝικά";
        final StringLiteral literal = new StringLiteral(s);
        assertThat(TokenVisitor.getToken(literal)).isEqualTo(s);
        assertThat(TokenVisitor.getNormalisedToken(literal)).isEqualTo("ελλη_νικά");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testColorLiteral(final boolean normalised) {
        final ColorLiteral color = new ColorLiteral(34, 7, 78);

        final String actual;
        if (normalised) {
            actual = TokenVisitor.getNormalisedToken(color);
        } else {
            actual = TokenVisitor.getToken(color);
        }

        assertThat(actual).isEqualTo("#22074e");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testRotationStyle(final boolean normalised) {
        final RotationStyle rotationStyle = new RotationStyle(RotationStyle.RotationStyleType.dont_rotate.getToken());

        final String actual;
        if (normalised) {
            actual = TokenVisitor.getNormalisedToken(rotationStyle);
        } else {
            actual = TokenVisitor.getToken(rotationStyle);
        }

        assertThat(actual).isEqualTo("dont_rotate");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "src/test/fixtures/allBlocks.json",
            "src/test/fixtures/ml_preprocessing/shared/pen_blocks.json",
            "src/test/fixtures/ml_preprocessing/shared/tts_blocks.json",
            "src/test/fixtures/ml_preprocessing/shared/music_blocks.json"
    })
    void testAllBlocksNoSpaces(final String filename) throws Exception {
        final Program program = getAST(filename);
        final NoSpacesChecker noSpacesChecker = new NoSpacesChecker();
        program.accept(noSpacesChecker);
    }

    static class NoSpacesChecker implements ScratchVisitor {
        @Override
        public void visit(ASTNode node) {
            final String token = TokenVisitor.getNormalisedToken(node);
            assertThat(token).doesNotContain(" ");

            visitChildren(node);
        }
    }
}
