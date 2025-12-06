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
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class RedundantAssignmentTest implements JsonTest, FinderTest {

    @Test
    public void testRedundantVariableAssignment() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set [SpriteLocalVariable v] to (1)
                set [SpriteLocalVariable v] to (2)""";
                
        Program program = parseScratchBlocks(script);
        
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        
        Assertions.assertEquals(1, issues.size(), "Should report one redundant assignment");
        
    }

    @Test
    public void testValidVariableAssignmentWithRead() throws IOException, ParsingException {
        // Assign, Read (Say), Assign. First assign is used.
        String script = """
                when green flag clicked
                set [SpriteLocalVariable v] to (1)
                say (SpriteLocalVariable)
                set [SpriteLocalVariable v] to (2)""";

        Program program = parseScratchBlocks(script);

        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(0, issues.size());
    }

    @Test
    public void testValidVariableAssignmentAtEnd() throws IOException, ParsingException {
        // Assign at end. Valid because it persists (Global/Sprite variable).
        String script = """
                when green flag clicked
                set [SpriteLocalVariable v] to (1)""";

        Program program = parseScratchBlocks(script);

        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(0, issues.size());
    }

    @Test
    public void testRedundantParameterAssignment() throws IOException, ParsingException {
        // Custom block with parameter p. Assign p twice.
        String script = """
                define foo (p)
                set [p v] to (1)
                set [p v] to (2)""";

        Program program = parseScratchBlocks(script);

        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(1, issues.size());
    }

    @Test
    public void testValidParameterAssignment() throws IOException, ParsingException {
        // Custom block with parameter p. Assign, Use.
        String script = """
                define foo (p)
                set [p v] to (1)
                change [p v] by (1)""";

        Program program = parseScratchBlocks(script);

        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(0, issues.size());
    }
    
    @Test
    public void testRedundantAssignmentInBranch() throws IOException, ParsingException {
        // If else where both branches overwrite without reading.
        String script = """
                when green flag clicked
                set [SpriteLocalVariable v] to (1)
                if <(1) = (1)> then
                  set [SpriteLocalVariable v] to (2)
                else
                  set [SpriteLocalVariable v] to (3)
                end""";
                
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        
        Assertions.assertEquals(1, issues.size());
    }

    @Test
    public void testNonRedundantAssignmentInBranch() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set [SpriteLocalVariable v] to (1)
                if <(SpriteLocalVariable) = (1)> then
                  set [SpriteLocalVariable v] to (2)
                end""";
                
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        
        Assertions.assertEquals(0, issues.size());
    }
    
    @Test
    public void testValidAssignmentInBranch() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set [SpriteLocalVariable v] to (1)
                if <(1) = (1)> then
                  set [SpriteLocalVariable v] to (2)
                end""";
                
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        
        Assertions.assertEquals(1, issues.size());
    }

    @Test
    public void testRedundantAttributeAssignment() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set x to (10)
                set x to (20)""";
                
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        
        Assertions.assertEquals(1, issues.size(), "Should report redundant attribute assignment");
    }

    @Test
    public void testValidAttributeAssignmentAtEnd() throws IOException, ParsingException {
        // set x to 10 at end. Should be valid (live at exit).
        String script = """
                when green flag clicked
                set x to (10)""";
                
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        
        Assertions.assertEquals(0, issues.size());
    }

    @Test
    public void testHideAfterHide() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                hide
                hide""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(1, issues.size(), "Should report redundant hide");
    }

    @Test
    public void testHideAfterShow() throws IOException, ParsingException {
        // Show then immediately Hide (without wait) makes Show redundant.
        String script = """
                when green flag clicked
                show
                hide""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(1, issues.size(), "Should report redundant show");
    }

    @Test
    public void testRedundantDirection() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                point in direction (90)
                point in direction (180)""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(1, issues.size(), "Should report redundant direction");
    }

    @Test
    public void testRedundantRotationStyle() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set rotation style [left-right v]
                set rotation style [all around v]""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(1, issues.size(), "Should report redundant rotation style");
    }

    @Test
    public void testRedundantDifferentEffects() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set [color v] effect to (10)
                set [fisheye v] effect to (20)""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant effect for different types");
    }

    @Test
    public void testRedundantSameEffect() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set [color v] effect to (10)
                set [color v] effect to (20)""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(1, issues.size(), "Should report redundant effect for same type");
    }

    @Test
    public void testSameEffectWithRendering() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set [color v] effect to (10)
                wait (1) seconds
                set [color v] effect to (20)""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant effect for same type");
    }

    @Test
    public void testRedundantStopAllSounds() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                stop all sounds
                stop all sounds""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(1, issues.size(), "Should report redundant stop all sounds");
    }

    @Test
    public void testValidHideWaitShow() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                show
                wait (1) seconds
                hide""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant show if interrupted by wait");
    }
    @Test
    public void testRedundantSoundEffect() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set [pitch v] effect to (100)
                set [pitch v] effect to (200)""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(1, issues.size(), "Should report redundant sound effect assignment");
    }

    @Test
    public void testDifferentSoundEffects() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set [pitch v] effect to (100)
                set [pan v] effect to (100)""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant assignment for different sound effects");
    }

    @Test
    public void testDistinctAttributeAssignments() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set x to (10)
                set y to (20)""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant assignment for x and y");
    }

    @Test
    public void testRedundantAssignmentInterruptedByHalt() throws IOException, ParsingException {
            String script = """
                when green flag clicked
                set x to (10)
                wait (1) seconds
                set x to (20)""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant assignment when interrupted by halt");
    }

    @Test
    public void testLoopAssignment() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                forever
                  set x to (10)
                end""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(1, issues.size(), "Should report redundant assignment for x");
    }

    @Test
    public void testLoopDynamicAssignment() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                forever
                  set x to (SpriteVariable)
                end""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant assignment for x");
    }

    @Test
    public void testTimesLoopAssignment() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                repeat (10)
                  set x to (10)
                end""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(1, issues.size(), "Should report redundant assignment for x");
    }

    @Test
    public void testUntilLoopAssignment() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                repeat until <(10) = (10)>
                  set x to (10)
                end""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(1, issues.size(), "Should report redundant assignment for x");
    }

    @Test
    public void testLoopAssignment2() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set x to (10)
                forever
                  set x to (10)
                end""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(2, issues.size(), "Should report redundant assignment for x");
    }

    @Test
    public void testTimesLoopAssignment2() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set x to (10)
                repeat (10)
                  set x to (10)
                end""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(2, issues.size(), "Should report redundant assignment for x");
    }

    @Test
    public void testUntilLoopAssignment2() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set x to (10)
                repeat until <(10) = (10)>
                  set x to (10)
                end""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(2, issues.size(), "Should report redundant assignment for x");
    }


    @Test
    public void testLoopAssignment3() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set [p v] to (1)
                set x to (10)
                forever
                  set x to (20)
                  change [p v] by (1)
                end""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(2, issues.size(), "Should report redundant assignment for x");
    }

    @Test
    public void testTimesLoopAssignment3() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set [p v] to (1)
                set x to (10)
                repeat (10)
                  set x to (20)
                  change [p v] by (1)
                end""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(2, issues.size(), "Should report redundant assignment for x");
    }

    @Test
    public void testUntilLoopAssignment3() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set [p v] to (1)
                set x to (10)
                repeat until <(p) = (10)>
                  set x to (20)
                  change [p v] by (1)
                end""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(2, issues.size(), "Should report redundant assignment for x");
    }

}
