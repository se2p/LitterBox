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
package de.uni_passau.fim.se2.litterbox.analytics.bugs;

import de.uni_passau.fim.se2.litterbox.FinderTest;
import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.uni_passau.fim.se2.litterbox.scratchblocks.ScratchBlocksParser;
import java.io.IOException;
import java.util.Set;

public class TypeErrorTest implements FinderTest, JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new TypeError(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testStringComparedToNumber() throws IOException, ParsingException {
        //must not find this, because this is part of ComparingLiterals
        assertThatFinderReports(0, new TypeError(), "./src/test/fixtures/bugs/stringComparedToNumber.json");
    }

    @Test
    public void testNumberComparedToString() throws IOException, ParsingException {
        assertThatFinderReports(1, new TypeError(), "./src/test/fixtures/bugs/numberComparedToString.json");
    }

    @Test
    public void testLoudnessComparedToNumber() throws IOException, ParsingException {
        assertThatFinderReports(0, new TypeError(), "./src/test/fixtures/bugs/compareLoudnessToNumber.json");
    }

    @Test
    public void testComplexComparison() throws IOException, ParsingException {
        assertThatFinderReports(1, new TypeError(), "./src/test/fixtures/bugs/complexComparison.json");
    }

    @Test
    public void testMotivation() throws IOException, ParsingException {
        assertThatFinderReports(1, new TypeError(), "./src/test/fixtures/bugs/motivation.json");
    }

    @Test
    public void testRedundantBooleanEquals() throws IOException, ParsingException {
        assertThatFinderReports(1, new TypeError(), "./src/test/fixtures/bugs/redundantBooleanEquals.json");
    }

    @Test
    public void testDistanceToWeirdCombination() throws IOException, ParsingException {
        Program booleanEquals = JsonTest.parseProgram("./src/test/fixtures/bugs/distanceToWeirdCombination.json");
        TypeError parameterName = new TypeError();
        Set<Issue> issues = parameterName.check(booleanEquals);
        Assertions.assertEquals(1, issues.size());
        for (Issue issue : issues) {
            String hint = issue.getHintText(translator);
            String weird = Hint.fromKey(TypeError.WEIRD_DISTANCE).getHintText(translator);
            String generic = Hint.fromKey(TypeError.NAME).getHintText(translator);
            Assertions.assertTrue(hint.equals(weird) || hint.equals(generic));
        }
    }

    @Test
    public void testNoTypeErrorVariable() throws IOException, ParsingException {
        assertThatFinderReports(0, new TypeError(), "./src/test/fixtures/bugs/noTypeError.json");
    }

    @Test
    public void testComparingTouching() throws IOException, ParsingException {
        Program booleanEquals = JsonTest.parseProgram("./src/test/fixtures/bugs/comparingTouching.json");
        TypeError parameterName = new TypeError();
        Set<Issue> issues = parameterName.check(booleanEquals);
        Assertions.assertEquals(1, issues.size());
        for (Issue issue : issues) {
            Assertions.assertNotNull(issue.getCodeLocation());
        }
    }
    @Test
    public void testAssignmentChecks() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        ScratchBlocksParser parser = new ScratchBlocksParser();
        String script = """
                when green flag clicked
                change [myVar v] by [hello]
                set x to <touching (mouse-pointer v)?>
                set size to (100) %
                """;
        prog = parser.extendProject(prog, "Sprite1", script);
        
        TypeError finder = new TypeError();
        Set<Issue> issues = finder.check(prog);
        
        // Expecting 2 issues: SetXTo <touching> AND ChangeVariableBy "hello" (StringLiteral).
        Assertions.assertEquals(2, issues.size());
    }

    @Test
    public void testBooleanChecks() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        ScratchBlocksParser parser = new ScratchBlocksParser();
        String script = """
                when green flag clicked
                if <touching (mouse-pointer v)?> then
                end
                if (50) then
                end
                """;
        prog = parser.extendProject(prog, "Sprite1", script);
        
        TypeError finder = new TypeError();
        Set<Issue> issues = finder.check(prog);
        
        // Expecting 1 issue: 1 for If(50). If<touching> is valid.
        Assertions.assertEquals(1, issues.size());
    }

    @Test
    public void testComprehensiveTypeChecks() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        ScratchBlocksParser parser = new ScratchBlocksParser();
        String script = """
                when green flag clicked
                
                // 1. Control Structures
                repeat until (50)
                end
                wait until <touching (mouse-pointer v)?>
                if (10) then
                else
                end

                // 2. Logical Operators
                if <not (10)> then
                end
                if <(10) and <mouse down?>> then
                end
                if <<key (space v) pressed?> or (10)> then
                end

                // 3. Sensing and Color
                if <touching color [#ff0000]?> then
                end
                if <color [#00ff00] is touching [#0000ff]?> then
                end
                
                // 4. Invalid Color
                wait until <touching color (50)?>
                """;
        prog = parser.extendProject(prog, "Sprite1", script);
        
        TypeError finder = new TypeError();
        Set<Issue> issues = finder.check(prog);
        
        // Expected Issues:
        // 1. Repeat Until (50) -> Number as Boolean (1 issue)
        // 2. If (10) -> Number as Boolean (1 issue)
        // 3. Not (10) -> Number as Boolean (1 issue)
        // 4. (10) and ... -> Number as Boolean (1 issue)
        // 5. ... or (10) -> Number as Boolean (1 issue)
        // Total: 5 issues. Touching Color (50) is valid (casted to Color).
        Assertions.assertEquals(5, issues.size());
    }
    @Test
    public void testColorChecks() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        ScratchBlocksParser parser = new ScratchBlocksParser();
        String script = """
                when green flag clicked
                set pen color to [#ff0000]
                set pen color to (50)
                """;
        prog = parser.extendProject(prog, "Sprite1", script);
        
        TypeError finder = new TypeError();
        Set<Issue> issues = finder.check(prog);
        
        Assertions.assertEquals(0, issues.size());
    }
}
