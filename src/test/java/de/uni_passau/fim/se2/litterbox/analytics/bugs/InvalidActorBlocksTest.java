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

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ParentVisitor;
import de.uni_passau.fim.se2.litterbox.scratchblocks.ScratchBlocksParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class InvalidActorBlocksTest implements JsonTest {

    private Program parseScratchBlocksOnStage(String code) throws ParsingException {
        Program baseProject = getAST("src/test/fixtures/emptyProject.json");
        ScratchBlocksParser parser = new ScratchBlocksParser();
        Program program = parser.extendProject(baseProject, "Stage", code);
        program.accept(new ParentVisitor());
        return program;
    }

    @Test
    public void testSpriteExclusive() throws ParsingException {
        assertThatFinderReports(48, new InvalidActorBlocks(), "./src/test/fixtures/smells/allSpriteExclusiveBlocksInStage.json");
    }

    @Test
    public void testStageExclusive() throws ParsingException {
        assertThatFinderReports(1, new InvalidActorBlocks(), "./src/test/fixtures/smells/allStageExclusiveBlocksInSprite.json");
    }

    // Tests for sprite-only blocks on Stage

    @Test
    public void testMotionOnStage() throws ParsingException {
        String script = """
                when green flag clicked
                move (10) steps""";

        Program program = parseScratchBlocksOnStage(script);
        InvalidActorBlocks finder = new InvalidActorBlocks();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(1, issues.size(), "Should report motion block on stage");
    }

    @Test
    public void testTurnOnStage() throws ParsingException {
        String script = """
                when green flag clicked
                turn cw (15) degrees""";

        Program program = parseScratchBlocksOnStage(script);
        InvalidActorBlocks finder = new InvalidActorBlocks();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(1, issues.size(), "Should report turn block on stage");
    }

    @Test
    public void testSayOnStage() throws ParsingException {
        String script = """
                when green flag clicked
                say [Hello!]""";

        Program program = parseScratchBlocksOnStage(script);
        InvalidActorBlocks finder = new InvalidActorBlocks();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(1, issues.size(), "Should report say block on stage");
    }

    @Test
    public void testThinkOnStage() throws ParsingException {
        String script = """
                when green flag clicked
                think [Hmm...]""";

        Program program = parseScratchBlocksOnStage(script);
        InvalidActorBlocks finder = new InvalidActorBlocks();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(1, issues.size(), "Should report think block on stage");
    }

    @Test
    public void testShowHideOnStage() throws ParsingException {
        String script = """
                when green flag clicked
                show
                hide""";

        Program program = parseScratchBlocksOnStage(script);
        InvalidActorBlocks finder = new InvalidActorBlocks();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(2, issues.size(), "Should report show and hide blocks on stage");
    }

    @Test
    public void testCostumeOnStage() throws ParsingException {
        String script = """
                when green flag clicked
                next costume""";

        Program program = parseScratchBlocksOnStage(script);
        InvalidActorBlocks finder = new InvalidActorBlocks();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(1, issues.size(), "Should report costume block on stage");
    }

    @Test
    public void testSizeOnStage() throws ParsingException {
        String script = """
                when green flag clicked
                set size to (100) %""";

        Program program = parseScratchBlocksOnStage(script);
        InvalidActorBlocks finder = new InvalidActorBlocks();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(1, issues.size(), "Should report size block on stage");
    }

    @Test
    public void testLayerOnStage() throws ParsingException {
        String script = """
                when green flag clicked
                go to [front v] layer""";

        Program program = parseScratchBlocksOnStage(script);
        InvalidActorBlocks finder = new InvalidActorBlocks();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(1, issues.size(), "Should report layer block on stage");
    }

    @Test
    public void testCreateCloneOfMyselfOnStage() throws ParsingException {
        String script = """
                when green flag clicked
                create clone of (myself v)""";

        Program program = parseScratchBlocksOnStage(script);
        InvalidActorBlocks finder = new InvalidActorBlocks();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(1, issues.size(), "Should report create clone of myself on stage");
    }

    @Test
    public void testCreateCloneOfSpriteOnStage() throws ParsingException {
        String script = """
                when green flag clicked
                create clone of [Sprite1 v]""";

        Program program = parseScratchBlocksOnStage(script);
        InvalidActorBlocks finder = new InvalidActorBlocks();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(0, issues.size(), "Should NOT report create clone of Sprite1 on stage");
    }

    @Test
    public void testDeleteCloneOnStage() throws ParsingException {
        String script = """
                when green flag clicked
                delete this clone""";

        Program program = parseScratchBlocksOnStage(script);
        InvalidActorBlocks finder = new InvalidActorBlocks();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(1, issues.size(), "Should report delete clone block on stage");
    }

    @Test
    public void testSpriteClickedOnStage() throws ParsingException {
        String script = """
                when this sprite clicked
                wait (1) seconds""";

        Program program = parseScratchBlocksOnStage(script);
        InvalidActorBlocks finder = new InvalidActorBlocks();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(1, issues.size(), "Should report sprite clicked event on stage");
    }

    @Test
    public void testStartedAsCloneOnStage() throws ParsingException {
        String script = """
                when I start as a clone
                wait (1) seconds""";

        Program program = parseScratchBlocksOnStage(script);
        InvalidActorBlocks finder = new InvalidActorBlocks();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(1, issues.size(), "Should report started as clone event on stage");
    }

    @Test
    public void testDragModeOnStage() throws ParsingException {
        String script = """
                when green flag clicked
                set drag mode [draggable v]""";

        Program program = parseScratchBlocksOnStage(script);
        InvalidActorBlocks finder = new InvalidActorBlocks();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(1, issues.size(), "Should report drag mode block on stage");
    }

    @Test
    public void testPenOnStage() throws ParsingException {
        String script = """
                when green flag clicked
                pen down
                pen up
                stamp
                set pen color to [#ff0000]
                set pen size to (1)
                change pen size by (1)
                set pen (brightness v) to (50)
                change pen (brightness v) by (10)""";

        Program program = parseScratchBlocksOnStage(script);
        InvalidActorBlocks finder = new InvalidActorBlocks();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(8, issues.size(), "Should report all pen blocks on stage");
    }

    @Test
    public void testReportersOnStage() throws ParsingException {
        String script = """
                when green flag clicked
                wait (x position) seconds
                wait (y position) seconds
                wait (direction) seconds
                wait (size) seconds
                wait (costume [number v]) seconds""";

        Program program = parseScratchBlocksOnStage(script);
        InvalidActorBlocks finder = new InvalidActorBlocks();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(5, issues.size(), "Should report sprite-specific reporters on stage");
    }

    // Tests for stage-only blocks on Sprite

    @Test
    public void testStageClickedOnSprite() throws ParsingException {
        String script = """
                when stage clicked
                wait (1) seconds""";

        Program program = parseScratchBlocks(script);
        InvalidActorBlocks finder = new InvalidActorBlocks();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(1, issues.size(), "Should report stage clicked event on sprite");
    }

    @Test
    public void testSwitchBackdropAndWaitOnSprite() throws ParsingException {
        String script = """
                when green flag clicked
                switch backdrop to (backdrop1 v) and wait""";

        Program program = parseScratchBlocks(script);
        InvalidActorBlocks finder = new InvalidActorBlocks();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(1, issues.size(), "Should report switch backdrop and wait on sprite");
    }

    // Tests for valid usage - no issues should be reported

    @Test
    public void testMotionOnSprite() throws ParsingException {
        String script = """
                when green flag clicked
                move (10) steps""";

        Program program = parseScratchBlocks(script);
        InvalidActorBlocks finder = new InvalidActorBlocks();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(0, issues.size(), "Should not report motion block on sprite");
    }

    @Test
    public void testStageClickedOnStage() throws ParsingException {
        String script = """
                when stage clicked
                wait (1) seconds""";

        Program program = parseScratchBlocksOnStage(script);
        InvalidActorBlocks finder = new InvalidActorBlocks();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(0, issues.size(), "Should not report stage clicked on stage");
    }

    @Test
    public void testEmptyProject() throws ParsingException {
        assertThatFinderReports(0, new InvalidActorBlocks(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testMultipleInvalidBlocksOnStage() throws ParsingException {
        String script = """
                when green flag clicked
                move (10) steps
                turn cw (15) degrees
                say [Hello!]
                hide""";

        Program program = parseScratchBlocksOnStage(script);
        InvalidActorBlocks finder = new InvalidActorBlocks();
        Set<Issue> issues = finder.check(program);

        Assertions.assertEquals(4, issues.size(), "Should report all invalid blocks on stage");
    }
}
