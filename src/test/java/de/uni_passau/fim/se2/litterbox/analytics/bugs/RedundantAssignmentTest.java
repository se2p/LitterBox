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
    public void testRedundantVariableAssignmentOfVariables() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set [SpriteLocalVariable v] to (OtherSpriteLocalVariable v)
                set [SpriteLocalVariable v] to (OtherSpriteLocalVariable v)""";

        Program program = parseScratchBlocks(script);

        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);

        Assertions.assertTrue(issues.isEmpty(), "Should NOT report one redundant assignment");
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
    public void testValidAssignmentInLoopBranch() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set [SpriteLocalVariable v] to (1)
                forever
                  if <(1) = (1)> then
                    set [SpriteLocalVariable v] to (2)
                  end
                end""";

        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant assignment because loop causes implicit thread switch");
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
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant assignment because loop causes thread switch (visualization)");
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
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant assignment because loop causes thread switch (visualization)");
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
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant assignment because loop causes thread switch (visualization)");
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
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant assignment because loop causes thread switch (visualization)");
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
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant assignment because loop causes thread switch (visualization)");
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
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant assignment because loop causes thread switch (visualization)");
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
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant assignment because loop causes thread switch (visualization)");
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
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant assignment because loop causes thread switch (visualization)");
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
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant assignment because loop causes thread switch (visualization)");
    }

    @Test
    public void testRedundantGoToPosXY() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                go to x: (10) y: (10)
                go to x: (20) y: (20)""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        // Expect 2 issues: one for x, one for y
        Assertions.assertEquals(2, issues.size(), "Should report redundant assignment for x and y");
    }

    @Test
    public void testDynamicGoToPosXY() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                forever
                  go to x: (mouse x) y: (mouse y)
                end""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant assignment for dynamic GoToPosXY");
    }

    @Test
    public void testRedundantSetDragMode() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set drag mode [draggable v]
                set drag mode [not draggable v]""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(1, issues.size(), "Should report redundant drag mode");
    }

    @Test
    public void testRedundantSetSizeTo() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set size to (50) %
                set size to (100) %""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(1, issues.size(), "Should report redundant set size");
    }

    @Test
    public void testInterruptedSetSizeTo() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set size to (50) %
                say [Hello] for (2) seconds
                set size to (100) %""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant set size");
    }

    @Test
    public void testDynamicSetSizeTo() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                forever
                  set size to (volume) %
                end""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant assignment for dynamic SetSizeTo");
    }

    @Test
    public void testRedundantClearGraphicEffects() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                clear graphic effects
                clear graphic effects""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertFalse(issues.isEmpty(), "Should report redundant clear graphic effects");
    }

    @Test
    public void testRedundantClearSoundEffects() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                clear sound effects
                clear sound effects""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertFalse(issues.isEmpty(), "Should report redundant clear sound effects");
    }

    @Test
    public void testRedundantSetVolumeTo() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set volume to (50) %
                set volume to (100) %""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(1, issues.size(), "Should report redundant set volume");
    }

    @Test
    public void testRedundantSetVolumeToVariable() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set volume to (SpriteLocalVariable v) %
                set volume to (SpriteLocalVariable v) %""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant set volume");
    }


    @Test
    public void testDynamicSetVolumeTo() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                forever
                  set volume to (loudness) %
                end""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant assignment for dynamic SetVolumeTo");
    }

    @Test
    public void testDynamicPointInDirection() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                forever
                  point in direction (direction)
                end""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant assignment for dynamic PointInDirection");
    }

    @Test
    public void testDynamicSetGraphicEffectTo() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                forever
                  set [color v] effect to (timer)
                end""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant assignment for dynamic SetGraphicEffectTo");
    }

    @Test
    public void testDynamicSetSoundEffectTo() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                forever
                  set [pitch v] effect to (volume)
                end""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant assignment for dynamic SetSoundEffectTo");
    }

    @Test
    public void testDynamicSwitchCostumeTo() throws IOException, ParsingException {
        // "switch costume to (timer)" or similar dynamic
        String script = """
                when green flag clicked
                forever
                  switch costume to (timer)
                end""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant assignment for dynamic SwitchCostumeTo");
    }

    @Test
    public void testDynamicSwitchBackdrop() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                forever
                  switch backdrop to (next backdrop v)
                end""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant assignment for dynamic SwitchBackdrop");
    }

    @Test
    public void testDynamicGoToPos() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                forever
                  go to (random position v)
                end""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant assignment for dynamic GoToPos");
    }

    @Test
    public void testDynamicPointTowards() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                forever
                  point towards (mouse-pointer v)
                end""";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant assignment for dynamic PointTowards");
    }

    @Test
    public void testRefinedMergeOverwritten() throws IOException, ParsingException {
        // Def 1 is Live (used on nested path), but on the overwrite path it is unused.
        // Def 2 is Live (used on its path).
        // Def 3 Overwrites both.
        // Def 1 should NOT be reported because Def 3 is reached by Def 2 as well.
        String script = """
                when green flag clicked
                if <(1) = (1)> then
                    set [x v] to (1)
                    if <(1) = (1)> then
                       wait (0.1) seconds
                    else
                       say (x)
                    end
                else
                    set [x v] to (2)
                    say (x)
                end
                set [x v] to (3)""";

        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(0, issues.size(), "Should verify false positive is fixed");
    }

    @Test
    public void testLoopWithConditionalUse() throws IOException, ParsingException {
        // This test case demonstrates the difference between old and new behavior.
        // Old Behavior: Reported redundant assignments because the variable is unused on the "else" (implicit) branch.
        // New Behavior: Suppresses the report because the variable IS used on the "if" branch, making it partially live.
        String script = """
                when green flag clicked
                set [x v] to (1)
                repeat (10)
                    if <key [space v] pressed?> then
                        say (x)
                    end
                    set [x v] to (2)
                end""";

        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(0, issues.size(), "Should suppress redundancy for partially dead store in loop");
    }

    @Test
    public void testLoopThreadSwitch() throws IOException, ParsingException {
        // "set x to 10" is followed by a loop (repeat 10).
        // Since the loop causes thread switches (halts), and "x" is a visual attribute (position),
        // it is implicitly "used" during the halt. Thus, the assignment should NOT be redundant.
        String script = """
                when green flag clicked
                set x to (10)
                repeat (10)
                end
                set x to (20)""";

        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant assignment because loop causes implicit thread switch");
    }

    @Test
    public void testSwitchCostumeFollowedByBroadcast() throws IOException, ParsingException {
        // This test verifies the fix for the interprocedural false positive:
        // A script with "switch costume" followed by "broadcast" should NOT be reported
        // as redundant, even if the receiving script also changes the costume.
        // The costume change is observable before the broadcast is sent.
        //
        // Note: The test fixture contains 1 legitimate redundant assignment in sprite "FA"'s
        // broadcast receiver script (two consecutive switch costume blocks). This is a real
        // bug and should be reported. The interprocedural false positives (costume before
        // broadcast) should NOT be reported.
        assertThatFinderReports(1, new RedundantAssignment(),
                "src/test/fixtures/bugs/broadcastCostumeFix.json");
    }

    @Test
    public void testSwitchCostumeSameScriptStillRedundant() throws IOException, ParsingException {
        // This test ensures that genuine redundant assignments (switch costume
        // followed by switch costume in the SAME script) are still detected.
        String script = """
                when green flag clicked
                switch costume to (costume1 v)
                switch costume to (costume2 v)""";

        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(1, issues.size(), "Should report redundant costume switch in same script");
    }

    @Test
    public void testVariableSetFollowedByBroadcast() throws IOException, ParsingException {
        // Variable assignments before broadcasts should also not be falsely reported.
        // The variable value is "used" by being visible/observable before the broadcast.
        String script = """
                when green flag clicked
                set [SpriteLocalVariable v] to (1)
                broadcast [message v]""";

        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant assignment before broadcast");
    }

    @Test
    public void testPositionSetFollowedByBroadcast() throws IOException, ParsingException {
        // Position changes before broadcasts are observable.
        String script = """
                when green flag clicked
                set x to (100)
                broadcast [message v]""";

        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant position assignment before broadcast");
    }

    @Test
    public void testCallStmtDoesNotCauseFalsePositive() throws IOException, ParsingException {
        // Similar to broadcast, custom block calls should not cause false positives
        // for assignments made before the call.
        String script = """
                define myblock
                set x to (200)

                when green flag clicked
                set x to (100)
                myblock""";

        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant assignment before custom block call");
    }
}
