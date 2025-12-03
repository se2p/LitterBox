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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.scratchblocks.ScratchBlocksParser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class InappropriateIntimacyTest implements JsonTest {

    @Test
    public void testInappropriateIntimacy() throws IOException, ParsingException {
        assertThatFinderReports(1, new InappropriateIntimacy(), "src/test/fixtures/smells/inappropriateIntimacy.json");
    }

    @Test
    void testLocalVariableCountsAsOwnAccess() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        ScratchBlocksParser parser = new ScratchBlocksParser();        
        String script = """
                when green flag clicked
                move (SpriteLocalVariable) steps
                move (SpriteLocalVariable) steps
                move (SpriteLocalVariable) steps
                say ([x position v] of (Stage v))
                say ([y position v] of (Stage v))
                """;
        
        Program newProgram = parser.extendProject(program, "Sprite1", script);
        
        InappropriateIntimacy finder = new InappropriateIntimacy();
        Set<Issue> issues = finder.check(newProgram);
        
        // ownAccesses: 3 (local vars)
        // otherSpriteAccesses: 2 (Stage)
        // 2 < 3 -> No smell.
        
        Assertions.assertTrue(issues.isEmpty(), "Smell should not be reported (own accesses increased)");
    }

    @Test
    void testLocalVariableComparisonFailsWhenNotEnough() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        ScratchBlocksParser parser = new ScratchBlocksParser();

        // 2 accesses to other sprite.
        // 1 access to local variable.
        // ownAccesses = 1.
        // 2 > 1 -> Smell reported.

        String script = """
                when green flag clicked
                move (SpriteLocalVariable) steps
                say ([x position v] of (Stage v))
                say ([y position v] of (Stage v))
                say ([direction v] of (Stage v))
                """;

        Program newProgram = parser.extendProject(program, "Sprite1", script);

        InappropriateIntimacy finder = new InappropriateIntimacy();
        Set<Issue> issues = finder.check(newProgram);

        Assertions.assertFalse(issues.isEmpty(), "Smell should be reported when local variable usage is not enough");
    }

    @Test
    void testMinAccessesThreshold() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        ScratchBlocksParser parser = new ScratchBlocksParser();
        
        // 1 access to other sprite.
        // ownAccesses = 0.
        // 1 < 2 (MIN_ACCESSES) -> No smell.
        
        String script = """
                when green flag clicked
                say ([x position v] of (Stage v))
                """;
        
        Program newProgram = parser.extendProject(program, "Sprite1", script);
        
        InappropriateIntimacy finder = new InappropriateIntimacy();
        Set<Issue> issues = finder.check(newProgram);
        
        Assertions.assertTrue(issues.isEmpty(), "Smell should not be reported (threshold not met)");
    }

    @Test
    void testSmellReported() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        ScratchBlocksParser parser = new ScratchBlocksParser();

        // 3 accesses to other sprite.
        // ownAccesses = 0.
        // 3 >= 2 (MIN_ACCESSES) and 3 > 0 -> Smell.

        String script = """
                when green flag clicked
                say ([x position v] of (Stage v))
                say ([y position v] of (Stage v))
                say ([volume v] of (Stage v))
                """;

        Program newProgram = parser.extendProject(program, "Sprite1", script);

        InappropriateIntimacy finder = new InappropriateIntimacy();
        Set<Issue> issues = finder.check(newProgram);

        Assertions.assertFalse(issues.isEmpty(), "Smell should be reported");
    }
}
