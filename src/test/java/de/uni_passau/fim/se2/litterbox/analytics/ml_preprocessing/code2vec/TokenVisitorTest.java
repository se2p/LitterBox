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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2vec;

import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class TokenVisitorTest {

    @Test
    public void testIntegerNumber() {
        NumberLiteral literal = new NumberLiteral(1234);
        TokenVisitor visitor = new TokenVisitor();
        literal.accept(visitor);
        assertThat(visitor.getToken()).isEqualTo("1234");
    }

    @Test
    public void testDoubleNumber() {
        NumberLiteral literal = new NumberLiteral(3.14);
        TokenVisitor visitor = new TokenVisitor();
        literal.accept(visitor);
        assertThat(visitor.getToken()).isEqualTo("3.14");
    }

    @Test
    public void testStringWithWhitespace() {
        StringLiteral literal = new StringLiteral("hello\t world \n");
        TokenVisitor visitor = new TokenVisitor();
        literal.accept(visitor);
        assertThat(visitor.getToken()).isEqualTo("helloworld");
    }

    @Test
    public void testStringWithComma() {
        StringLiteral literal = new StringLiteral("a,b,c");
        TokenVisitor visitor = new TokenVisitor();
        literal.accept(visitor);
        assertThat(visitor.getToken()).isEqualTo("abc");
    }
}
