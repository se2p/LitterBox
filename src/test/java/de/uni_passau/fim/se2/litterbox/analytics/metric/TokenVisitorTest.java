/*
 * Copyright (C) 2019-2021 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

public class TokenVisitorTest  implements JsonTest {

    @Test
    public void testFourBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/metrics/entropy_3identical.json");
        TokenVisitor visitor = new TokenVisitor();
        program.accept(visitor);

        assertThat(visitor.getTotalTokenCount()).isEqualTo(7);
        assertThat(visitor.getUniqueTokens().size()).isEqualTo(3);
    }

    @Test
    public void testNestedBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/metrics/entropy_nestedblocks.json");
        TokenVisitor visitor = new TokenVisitor();
        program.accept(visitor);

        assertThat(visitor.getTotalTokenCount()).isEqualTo(11);
        assertThat(visitor.getUniqueTokens().size()).isEqualTo(7);
    }

    @Test
    public void testCustomBlock() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/metrics/entropy_customblock.json");
        TokenVisitor visitor = new TokenVisitor();
        program.accept(visitor);

        assertThat(visitor.getTotalTokenCount()).isEqualTo(23);
        assertThat(visitor.getUniqueTokens().size()).isEqualTo(19);
    }


    @Test
    public void testIfElse() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/cfg/ifelse_repeattimes.json");
        TokenVisitor visitor = new TokenVisitor();
        program.accept(visitor);

        assertThat(visitor.getTotalTokenCount()).isEqualTo(12);
        assertThat(visitor.getUniqueTokens().size()).isEqualTo(7);
    }

    @Test
    public void testMultipleScripts() throws IOException, ParsingException {
        Program program = JsonTest.parseProgram("./src/test/fixtures/weightedMethod.json");
        TokenVisitor visitor = new TokenVisitor();
        program.accept(visitor);

        assertThat(visitor.getTotalTokenCount()).isEqualTo(14);
        assertThat(visitor.getUniqueTokens().size()).isEqualTo(14);
    }

    @Test
    public void testComplexScript() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/metrics/oneMoreComplex.json");
        TokenVisitor visitor = new TokenVisitor();
        program.accept(visitor);

        assertThat(visitor.getTotalTokenCount()).isEqualTo(11);
        assertThat(visitor.getUniqueTokens().size()).isEqualTo(10);
    }


}
