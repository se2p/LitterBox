/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
import java.util.Locale;
import java.util.Set;

public class TypeErrorTest implements FinderTest, JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new TypeError(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testStringComparedToNumber() throws IOException, ParsingException {
        // Now consistent with NumberComparedToString
        assertThatFinderReports(1, new TypeError(), "./src/test/fixtures/bugs/stringComparedToNumber.json");
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
        assertThatFinderReports(2, new TypeError(), "./src/test/fixtures/bugs/complexComparison.json");
    }

    @Test
    public void testMotivation() throws IOException, ParsingException {
        assertThatFinderReports(0, new TypeError(), "./src/test/fixtures/bugs/motivation.json");
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
        // Expecting 2 issues: each Touching block (Boolean) is wrapped in AsNumber by the parser,
        // and the type checker correctly flags each as a type error (Boolean where Number expected).
        Assertions.assertEquals(2, issues.size());
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
        // TODO check this
        TypeError finder = new TypeError();
        Set<Issue> issues = finder.check(prog);
        
        // Expecting 0 issues for correct Hex/RGB usage.
        Assertions.assertEquals(0, issues.size());
    }

    @Test
    public void testPenColorTypeErrors() throws IOException, ParsingException {
        ScratchBlocksParser parser = new ScratchBlocksParser();
        Program prog = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        // 1. set pen color to <touching mouse?> (Boolean instead of Color/Number)
        // 2. set pen color to (50) (Number) - Should be VALID (hue) or at least not strictly invalid like Boolean.
        String script = """
                when green flag clicked
                set pen color to <touching (mouse-pointer v)?>
                set pen color to (50)
                """;
        prog = parser.extendProject(prog, "Sprite1", script);
        
        TypeError finder = new TypeError();
        Set<Issue> issues = finder.check(prog);
        
        // Expect 1 issue: Boolean usage. Number usage is acceptable for pen color (sets hue).
        Assertions.assertEquals(1, issues.size(), "Should report error for Boolean used as Pen Color");
    }

    @Test
    public void testBinaryOperatorReturnTypes() throws IOException, ParsingException {
        ScratchBlocksParser parser = new ScratchBlocksParser();
        Program prog = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        // Verify that binary math operators return Number type, and cause errors when compared to String.
        // If they returned ANY or null, these comparisons might be ignored.
        String script = """
                when green flag clicked
                if <((5) + (5)) = [string]> then
                end
                if <((5) - (5)) = [string]> then
                end
                if <((5) * (5)) = [string]> then
                end
                if <((5) / (5)) = [string]> then
                end
                """;
        
        prog = parser.extendProject(prog, "Sprite1", script);
        
        TypeError finder = new TypeError();
        Set<Issue> issues = finder.check(prog);
        
        // Expect 4 issues: One for each comparison of Number (result of math op) vs String.
        StringBuilder issuesStr = new StringBuilder();
        for(Issue issue : issues) {
             issuesStr.append(issue.getCodeLocation()).append(" ").append(issue.getHintText(FinderTest.translator)).append("\\n");
        }
        Assertions.assertEquals(4, issues.size(), "Math operators should return Number type. Found:\\n" + issuesStr);
    }


    @Test
    public void testVariableTypes() throws IOException, ParsingException {
        ScratchBlocksParser parser = new ScratchBlocksParser();
        Program prog;
        TypeError finder = new TypeError();
        Set<Issue> issues;

        // 1. Valid Number Assignment
        prog = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        prog = parser.extendProject(prog, "Sprite1", """
                when green flag clicked
                set [myVar v] to (10)
                move (myVar) steps
                """);
        issues = finder.check(prog);
        Assertions.assertEquals(0, issues.size(), "Should verify numeric variable usage");

        // 2. Invalid String Assignment
        prog = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        prog = parser.extendProject(prog, "Sprite1", """
                when green flag clicked
                set [myVar v] to [hello]
                move (myVar) steps
                """);
        issues = finder.check(prog);
        Assertions.assertEquals(1, issues.size(), "Should report String variable used as Number");

        // 3. Valid Answer (ANY)
        prog = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        prog = parser.extendProject(prog, "Sprite1", """
                when green flag clicked
                set [myVar v] to (answer)
                move (myVar) steps
                """);
        issues = finder.check(prog);
        Assertions.assertEquals(0, issues.size(), "Answer should be compatible with Number");

        // 4. Incompatible Boolean usage
        prog = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        prog = parser.extendProject(prog, "Sprite1", """
                when green flag clicked
                set [myVar v] to (10)
                if (myVar) then
                end
                """);
        issues = finder.check(prog);
        Assertions.assertEquals(1, issues.size(), "Number variable incompatible with Boolean context");
    }


    @Test
    public void testLoopControlFlow() throws IOException, ParsingException {
        // Test that type persists through a loop
        ScratchBlocksParser parser = new ScratchBlocksParser();
        Program prog = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        prog = parser.extendProject(prog, "Sprite1", """
                when green flag clicked
                set [myVar v] to (10)
                repeat (10)
                    move (myVar) steps
                    set [myVar v] to (myVar)
                end
                if (myVar) then
                end
                """);
        TypeError finder = new TypeError();
        Set<Issue> issues = finder.check(prog);
        // Expect 1 issue: myVar (Number) used as Boolean in 'if'
        // 'move (myVar)' should be valid.
        Assertions.assertEquals(1, issues.size(), "Loop should propagate number type");
    }

    @Test
    public void testTypeConflicts() throws IOException, ParsingException {
        // Test reassignment to incompatible type
        ScratchBlocksParser parser = new ScratchBlocksParser();
        Program prog = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        // Path 1: Set to Number. Path 2: Set to String. Join: ? -> TOP
        // But we can't easily make divergent paths merge without if/else.
        // Simple reassignment:
        prog = parser.extendProject(prog, "Sprite1", """
                when green flag clicked
                set [myVar v] to (10)
                set [myVar v] to [hello]
                move (myVar) steps
                """);
        TypeError finder = new TypeError();
        Set<Issue> issues = finder.check(prog);
        // Expect 1 issue: second move uses String
        Assertions.assertEquals(1, issues.size(), "Reassignment should change type to String");
    }

    @Test
    public void testInterScript() throws IOException, ParsingException {
        // Test variables in different scripts (same sprite -> shared scope)
        ScratchBlocksParser parser = new ScratchBlocksParser();
        Program prog = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        prog = parser.extendProject(prog, "Sprite1", """
                when green flag clicked
                set [myVar v] to (10)
                """);
        // Second script uses it. Dataflow currently does NOT propagate between scripts (parallel execution undefined).
        // Each script starts with empty facts (or initial facts).
        // So myVar in script 2 should be BOTTOM (uninitialized) or ANY?
        // Our init logic: initializeMay() sets empty set.
        // getVariableType returns BOTTOM if not found.
        // BOTTOM matches everything in isCompatible? No, BOTTOM means uninitialized.
        // isCompatible(NUMBER, BOTTOM) -> true (allow uninitialized to avoid noise?)
        // Let's check isCompatible:
        // if (actual == Type.ANY || actual == Type.TOP || actual == Type.BOTTOM) return true;
        // So it should be valid.
        
        prog = parser.extendProject(prog, "Sprite1", """
                when key [space v] pressed
                move (myVar) steps
                """);
                
        TypeError finder = new TypeError();
        Set<Issue> issues = finder.check(prog);
        Assertions.assertEquals(0, issues.size(), "Cross-script usage should be assumed valid (BOTTOM/ANY)");
    }
    @Test
    public void testNonBooleanExpectations() throws IOException, ParsingException {
        ScratchBlocksParser parser = new ScratchBlocksParser();
        Program prog = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        // Test blocks that expect non-boolean types (Number, String) getting incompatible types
        String script = """
                when green flag clicked
                // 1. Move steps expects Number, gets Boolean (Touching)
                move <touching (mouse-pointer v)?> steps
                
                // 2. Set variable expects Number/String, but usage context might assume Number.
                // 'set x to' explicitly expects Number. gets Boolean (Key Pressed)
                set x to <key (space v) pressed?>
                
                // 3. Wait seconds expects Number, gets Boolean (Equals)
                wait <(1) = (1)> secs
                
                // 4. Change size by expects Number, gets String literal
                change size by [some string]
                
                // 5. Change volume by expects Number, gets String literal
                change volume by [loud]
                """;
        
        prog = parser.extendProject(prog, "Sprite1", script);
        
        TypeError finder = new TypeError();
        Set<Issue> issues = finder.check(prog);
        
        // Expected issues: 5
        StringBuilder issuesStr = new StringBuilder();
        for(Issue issue : issues) {
             issuesStr.append(issue.getCodeLocation()).append(" ").append(issue.getHintText(FinderTest.translator)).append("\n");
        }
        Assertions.assertEquals(5, issues.size(), "Should report type errors for non-boolean expectations. Found:\n" + issuesStr);
    }



    @Test
    public void testTypeMisuses() throws IOException, ParsingException {
        ScratchBlocksParser parser = new ScratchBlocksParser();
        Program prog = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        // We construct a script with type errors for Distance, Loudness, Position, and Direction
        String script = """
                when green flag clicked
                // 1. Distance misuse (Number as Boolean)
                if (distance to (mouse-pointer v)) then
                end
                
                // 2. Loudness misuse
                if (loudness) then
                end
                // Loudness compared to String
                if <(loudness) = [string]> then
                end

                // 3. Position misuse
                if (x position) then
                end
                if (y position) then
                end
                if <(x position) = [string]> then
                end
                
                // 4. Direction misuse
                if (direction) then
                end
                if <(direction) = [string]> then
                end
                """;
        
        prog = parser.extendProject(prog, "Sprite1", script);
        
        TypeError finder = new TypeError();
        Set<Issue> issues = finder.check(prog);
        
        // Total expected: 8
        StringBuilder issuesStr = new StringBuilder();
        for(Issue issue : issues) {
             issuesStr.append(issue.getCodeLocation()).append(" ").append(issue.getHintText(FinderTest.translator)).append("\n");
        }
        Assertions.assertEquals(8, issues.size(), "Should report type errors for all misuses. Found:\n" + issuesStr);
    }
    @Test
    public void testPickRandomMisuse() throws IOException, ParsingException {
        ScratchBlocksParser parser = new ScratchBlocksParser();
        Program prog = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        String script = """
                when green flag clicked
                // 1. Boolean context misuse
                if (pick random (1) to (10)) then
                end
                
                // 2. Comparison with String
                if <(pick random (1) to (10)) = [string]> then
                end
                """;
        
        prog = parser.extendProject(prog, "Sprite1", script);
        
        TypeError finder = new TypeError();
        Set<Issue> issues = finder.check(prog);
        
        
        StringBuilder issuesStr = new StringBuilder();
        for(Issue issue : issues) {
             issuesStr.append(issue.getCodeLocation()).append(" ").append(issue.getHintText(FinderTest.translator)).append("\n");
        }
        Assertions.assertEquals(2, issues.size(), "Should report type errors for PickRandom misuse. Found:\n" + issuesStr);
    }

    @Test
    public void testPickRandomArgumentTypes() throws IOException, ParsingException {
        ScratchBlocksParser parser = new ScratchBlocksParser();
        Program prog = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        // Script 1: pick random [string] to (10) -> Error on 1st arg
        // Script 2: pick random (1) to <boolean> -> Error on 2nd arg
        String script = """
                when green flag clicked
                set [myVar v] to (pick random [string] to (10))
                set [myVar v] to (pick random (1) to <touching (mouse-pointer v)?>)
                """;
        
        prog = parser.extendProject(prog, "Sprite1", script);
        
        TypeError finder = new TypeError();
        Set<Issue> issues = finder.check(prog);
        
        // Expect 2 issues: String as Number, Boolean as Number.
        StringBuilder issuesStr = new StringBuilder();
        for(Issue issue : issues) {
             issuesStr.append(issue.getCodeLocation()).append(" ").append(issue.getHintText(FinderTest.translator)).append("\n");
        }
        Assertions.assertEquals(2, issues.size(), "Should report errors for invalid PickRandom arguments. Found:\n" + issuesStr);
    }

    @Test
    public void testParameterTypeChecks() throws IOException, ParsingException {
        // This test uses a manual JSON fixture because constructing custom blocks with parameters via string extension is complex/not supported by parser yet.
        // Fixture contains:
        // define "my block %s %b" (number param, boolean param)
        //   if <number param> then  <- Issue 1: Number used as Boolean
        //   end
        //   move <boolean param> steps <- Issue 2: Boolean used as Number
        
        assertThatFinderReports(2, new TypeError(), "./src/test/fixtures/bugs/parameterTypeCheck.json");
    }

    @Test
    public void testParameterCall() throws IOException, ParsingException {
        assertThatFinderReports(1, new TypeError(), "./src/test/fixtures/bugs/parameterTypeCheckWithCall.json");
    }

    @Test
    public void testDetailedHintContent() throws IOException, ParsingException {
        Locale defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        try {
            ScratchBlocksParser parser = new ScratchBlocksParser();
            Program prog = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
            // Script with a type mismatch: if (50) then (Number where Boolean expected)
            String script = """
                    when green flag clicked
                    if (50) then
                    end
                    """;
            prog = parser.extendProject(prog, "Sprite1", script);
            
            TypeError finder = new TypeError();
            Set<Issue> issues = finder.check(prog);
            
            Assertions.assertEquals(1, issues.size());
            Issue issue = issues.iterator().next();
            String hintText = issue.getHintText(translator);
            
            // Check for specific parts of the hint
            Assertions.assertTrue(hintText.contains("Boolean (True/False)"), "Hint should contain expected type 'Boolean (True/False)'. Got: " + hintText);
            Assertions.assertTrue(hintText.contains("Number"), "Hint should contain actual type 'Number'. Got: " + hintText);
                
            // Test comparison hint
            prog = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
            script = """
                    when green flag clicked
                    if <(50) = [hello]> then
                    end
                    """;
            prog = parser.extendProject(prog, "Sprite1", script);
            issues = finder.check(prog);
            
            Assertions.assertEquals(1, issues.size());
            issue = issues.iterator().next();
            hintText = issue.getHintText(translator);
            
            Assertions.assertTrue(hintText.contains("Number"), "Hint should contain left type 'Number'. Got: " + hintText);
            Assertions.assertTrue(hintText.contains("Text"), "Hint should contain right type 'Text'. Got: " + hintText);
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }
    @Test
    public void testEmptyHoles() throws IOException, ParsingException {
        ScratchBlocksParser parser = new ScratchBlocksParser();
        Program prog = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        String script = """
                when green flag clicked
                if <> then
                end
                wait until <>
                move () steps
                if <() = ()> then
                end
                """;
        
        prog = parser.extendProject(prog, "Sprite1", script);
        
        TypeError finder = new TypeError();
        Set<Issue> issues = finder.check(prog);
                
        Assertions.assertEquals(0, issues.size(), "Should suppress type errors for empty holes.");
    }
}

