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
        // Double assignment to SpriteLocalVariable
        String script = "when green flag clicked\n" +
                "set [SpriteLocalVariable v] to (1)\n" +
                "set [SpriteLocalVariable v] to (2)";
                
        Program program = parseScratchBlocks(script);
        
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        
        // Should have 1 issue for the first assignment
        Assertions.assertEquals(1, issues.size(), "Should report one redundant assignment");
        
    }

    @Test
    public void testValidVariableAssignmentWithRead() throws IOException, ParsingException {
        // Assign, Read (Say), Assign. First assign is used.
        String script = "when green flag clicked\n" +
                "set [SpriteLocalVariable v] to (1)\n" +
                "say (SpriteLocalVariable)\n" +
                "set [SpriteLocalVariable v] to (2)";

        Program program = parseScratchBlocks(script);

        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(0, issues.size());
    }

    @Test
    public void testValidVariableAssignmentAtEnd() throws IOException, ParsingException {
        // Assign at end. Valid because it persists (Global/Sprite variable).
        String script = "when green flag clicked\n" +
                "set [SpriteLocalVariable v] to (1)";

        Program program = parseScratchBlocks(script);

        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(0, issues.size());
    }

    @Test
    public void testRedundantParameterAssignment() throws IOException, ParsingException {
        // Custom block with parameter p. Assign p twice.
        String script = "define foo (p)\n" +
                "set [p v] to (1)\n" +
                "set [p v] to (2)";

        Program program = parseScratchBlocks(script);

        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(1, issues.size());
    }

    @Test
    public void testValidParameterAssignment() throws IOException, ParsingException {
        // Custom block with parameter p. Assign, Use.
        String script = "define foo (p)\n" +
                "set [p v] to (1)\n" +
                "change [p v] by (1)";

        Program program = parseScratchBlocks(script);

        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(0, issues.size());
    }
    
    @Test
    public void testRedundantAssignmentInBranch() throws IOException, ParsingException {
        // If else where both branches overwrite without reading.
        // x = 1. if true: x=2 else x=3.
        // x=1 is dead.
        String script = "when green flag clicked\n" +
                "set [SpriteLocalVariable v] to (1)\n" +
                "if <(1) = (1)> then\n" +
                "  set [SpriteLocalVariable v] to (2)\n" +
                "else\n" +
                "  set [SpriteLocalVariable v] to (3)\n" +
                "end";
                
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        
        Assertions.assertEquals(1, issues.size());
    }

    @Test
    public void testNonRedundantAssignmentInBranch() throws IOException, ParsingException {
        String script = "when green flag clicked\n" +
                "set [SpriteLocalVariable v] to (1)\n" +
                "if <(SpriteLocalVariable) = (1)> then\n" +
                "  set [SpriteLocalVariable v] to (2)\n" +
                "end";
                
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        
        Assertions.assertEquals(0, issues.size());
    }
    
    @Test
    public void testValidAssignmentInBranch() throws IOException, ParsingException {
        // x = 1. if true: x=2 else (nothing).
        // else branch reaches end. x=1 is live at end. So x=1 is NOT dead.
        String script = "when green flag clicked\n" +
                "set [SpriteLocalVariable v] to (1)\n" +
                "if <(1) = (1)> then\n" +
                "  set [SpriteLocalVariable v] to (2)\n" +
                "end";
                
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        
        Assertions.assertEquals(0, issues.size());
    }

    @Test
    public void testRedundantAttributeAssignment() throws IOException, ParsingException {
        // Redundant x position assignment
        // set x to 10. set x to 20.
        String script = "when green flag clicked\n" +
                "set x to (10)\n" +
                "set x to (20)";
                
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        
        Assertions.assertEquals(1, issues.size(), "Should report redundant attribute assignment");
    }

    @Test
    public void testValidAttributeAssignmentAtEnd() throws IOException, ParsingException {
        // set x to 10 at end. Should be valid (live at exit).
        String script = "when green flag clicked\n" +
                "set x to (10)";
                
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        
        Assertions.assertEquals(0, issues.size());
    }

    @Test
    public void testHideAfterHide() throws IOException, ParsingException {
        String script = "when green flag clicked\n" +
                "hide\n" +
                "hide";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(1, issues.size(), "Should report redundant hide");
    }

    @Test
    public void testHideAfterShow() throws IOException, ParsingException {
        // Show then immediately Hide (without wait) makes Show redundant.
        String script = "when green flag clicked\n" +
                "show\n" +
                "hide";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(1, issues.size(), "Should report redundant show");
    }

    @Test
    public void testRedundantDirection() throws IOException, ParsingException {
        String script = "when green flag clicked\n" +
                "point in direction (90)\n" +
                "point in direction (180)";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(1, issues.size(), "Should report redundant direction");
    }

    @Test
    public void testRedundantRotationStyle() throws IOException, ParsingException {
        // set rotation style [left-right v]; set rotation style [all around v]
        String script = "when green flag clicked\n" +
                "set rotation style [left-right v]\n" +
                "set rotation style [all around v]";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(1, issues.size(), "Should report redundant rotation style");
    }

    @Test
    public void testRedundantDifferentEffects() throws IOException, ParsingException {
        // set [color v] effect to (10); set [fisheye v] effect to (20)
        // Should NOT be redundant
        String script = "when green flag clicked\n" +
                "set [color v] effect to (10)\n" +
                "set [fisheye v] effect to (20)";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant effect for different types");
    }

    @Test
    public void testRedundantSameEffect() throws IOException, ParsingException {
        // set [color v] effect to (10); set [color v] effect to (20)
        String script = "when green flag clicked\n" +
                "set [color v] effect to (10)\n" +
                "set [color v] effect to (20)";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(1, issues.size(), "Should report redundant effect for same type");
    }

    @Test
    public void testSameEffectWithRendering() throws IOException, ParsingException {
        // set [color v] effect to (10); set [color v] effect to (20)
        String script = "when green flag clicked\n" +
                "set [color v] effect to (10)\n" +
                "wait (1) seconds\n" +
                "set [color v] effect to (20)";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant effect for same type");
    }

    @Test
    public void testRedundantStopAllSounds() throws IOException, ParsingException {
        // stop all sounds; stop all sounds
        String script = "when green flag clicked\n" +
                "stop all sounds\n" +
                "stop all sounds";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(1, issues.size(), "Should report redundant stop all sounds");
    }

    @Test
    public void testValidHideWaitShow() throws IOException, ParsingException {
        // show; wait 1 secs; hide -> NOT redundant
        String script = "when green flag clicked\n" +
                "show\n" +
                "wait (1) seconds\n" +
                "hide";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant show if interrupted by wait");
    }
    @Test
    public void testRedundantSoundEffect() throws IOException, ParsingException {
        // set [pitch v] effect to (100); set [pitch v] effect to (200)
        String script = "when green flag clicked\n" +
                "set [pitch v] effect to (100)\n" +
                "set [pitch v] effect to (200)";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(1, issues.size(), "Should report redundant sound effect assignment");
    }

    @Test
    public void testDifferentSoundEffects() throws IOException, ParsingException {
        // set [pitch v] effect to (100); set [pan v] effect to (100)
        String script = "when green flag clicked\n" +
                "set [pitch v] effect to (100)\n" +
                "set [pan v] effect to (100)";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant assignment for different sound effects");
    }

    @Test
    public void testDistinctAttributeAssignments() throws IOException, ParsingException {
        // set x to (10); set y to (20)
        String script = "when green flag clicked\n" +
                "set x to (10)\n" +
                "set y to (20)";
        Program program = parseScratchBlocks(script);
        RedundantAssignment finder = new RedundantAssignment();
        Set<Issue> issues = finder.check(program);
        Assertions.assertEquals(0, issues.size(), "Should NOT report redundant assignment for x and y");
    }
}
