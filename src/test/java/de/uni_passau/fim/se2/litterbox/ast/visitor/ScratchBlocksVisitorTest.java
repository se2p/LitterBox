/*
 * Copyright (C) 2020 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.*;
import de.uni_passau.fim.se2.litterbox.analytics.smells.DeadCode;
import de.uni_passau.fim.se2.litterbox.analytics.smells.UnusedVariable;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScratchBlocksVisitorTest implements JsonTest {

    @Test
    public void testMotionBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/motionblocks.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "move (10) steps" + System.lineSeparator() +
                "turn right (15) degrees" + System.lineSeparator() +
                "turn left (15) degrees" + System.lineSeparator() +
                "go to (random position v)" + System.lineSeparator() +
                "go to x: (0) y: (0)" + System.lineSeparator() +
                "glide (1) secs to (random position v)" + System.lineSeparator() +
                "glide (1) secs to x: (0) y: (0)" + System.lineSeparator() +
                "point in direction (90)" + System.lineSeparator() +
                "point towards (mouse-pointer v)" + System.lineSeparator() +
                "change x by (10)" + System.lineSeparator() +
                "set x to (0)" + System.lineSeparator() +
                "change y by (10)" + System.lineSeparator() +
                "set y to (0)" + System.lineSeparator() +
                "if on edge, bounce" + System.lineSeparator() +
                "set rotation style [left-right v]" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testTouchingEdgeBlock() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/touchingedgeblock.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "forever " + System.lineSeparator() +
                "if <touching (edge v) ?> then" + System.lineSeparator() +
                "say [Hello!] for (2) seconds" + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testTouchingSpriteBlock() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/touchingspriteblock.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "forever " + System.lineSeparator() +
                "if <touching (Bell v) ?> then" + System.lineSeparator() +
                "say [Hello!]" + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(), result);
    }

    @Test
    public void testTouchingVarBlock() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/touchingvarblock.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "set [my variable v] to [Bell]" + System.lineSeparator() +
                "forever " + System.lineSeparator() +
                "if <touching (my variable) ?> then" + System.lineSeparator() +
                "say [Hello!]" + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(), result);
    }

    @Test
    public void testSetVarToVar() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/setvariabletovariable.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "set [my variable v] to (other variable)" + System.lineSeparator() +
                "change [my variable v] by (other variable)" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(), result);
    }

    @Test
    public void testSetVarToBool() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/booleaninsetvar.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator()+
                "define block name [number or text] <boolean>" + System.lineSeparator()+
                "set [my variable v] to <touching color [#c9dae2] ?>" + System.lineSeparator()+
                "set [my variable v] to <touching (mouse-pointer v) ?>" + System.lineSeparator()+
                "set [my variable v] to <color [#8d6b27] is touching [#805a3c] ?>" + System.lineSeparator()+
                "set [my variable v] to <key (space v) pressed?>" + System.lineSeparator()+
                "set [my variable v] to <mouse down?>" + System.lineSeparator()+
                "set [my variable v] to <<> and <>>" + System.lineSeparator()+
                "set [my variable v] to <[apple] contains [a]?>" + System.lineSeparator()+
                "set [my variable v] to <not <>>" + System.lineSeparator()+
                "set [my variable v] to <<> or <>>" + System.lineSeparator()+
                "set [my variable v] to <[] > (50)>" + System.lineSeparator()+
                "set [my variable v] to <boolean>" + System.lineSeparator()+
                "[/scratchblocks]" + System.lineSeparator(), result);
    }

    @Test
    public void testSetVarToAllOtherblocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/setvartoblocks.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "set [my variable v] to (0)" + System.lineSeparator() +
                "set [my variable v] to (other variable)" + System.lineSeparator() +
                "set [my variable v] to (answer)" + System.lineSeparator() +
                "set [my variable v] to (distance to (mouse-pointer v))" + System.lineSeparator() +
                "set [my variable v] to (join [apple ][banana])" + System.lineSeparator() +
                "set [my variable v] to (length of [thelist v])" + System.lineSeparator() +
                "set [my variable v] to ([abs v] of (my variable))" + System.lineSeparator() +
                "set [my variable v] to ((my variable) mod (other variable))" + System.lineSeparator() +
                "set [my variable v] to (round (my variable))" + System.lineSeparator() +
                "set [my variable v] to (pick random (1) to (10))" + System.lineSeparator() +
                "set [my variable v] to ([my variable v] of (Stage v)?)" + System.lineSeparator() +
                "set [my variable v] to ((10)+(my variable))" + System.lineSeparator() +
                "set [my variable v] to (item (1) of [thelist v])" + System.lineSeparator() +
                "set [my variable v] to (item # of [thing] in [thelist v])" + System.lineSeparator() +
                "set [my variable v] to (username)" + System.lineSeparator() +
                "set [my variable v] to (thelist)" + System.lineSeparator() +
                "set [my variable v] to (mouse x)" + System.lineSeparator() +
                "set [my variable v] to (loudness)" + System.lineSeparator() +
                "set [my variable v] to (days since 2000)" + System.lineSeparator() +
                "set [my variable v] to (current (year v))" + System.lineSeparator() +
                "set [my variable v] to (timer)" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(), result);
    }

    @Test
    public void testChangeVarToAllOtherblocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/changevartoblocks.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "change [my variable v] by (1)" + System.lineSeparator() +
                "change [my variable v] by (x position)" + System.lineSeparator() +
                "change [my variable v] by (y position)" + System.lineSeparator() +
                "change [my variable v] by (backdrop [number v])" + System.lineSeparator() +
                "change [my variable v] by (costume [number v])" + System.lineSeparator() +
                "change [my variable v] by (direction)" + System.lineSeparator() +
                "change [my variable v] by (size)" + System.lineSeparator() +
                "change [my variable v] by (volume)" + System.lineSeparator() +
                "change [my variable v] by (other variable)" + System.lineSeparator() +
                "change [my variable v] by (answer)" + System.lineSeparator() +
                "change [my variable v] by (distance to (mouse-pointer v))" + System.lineSeparator() +
                "change [my variable v] by (join [apple ][banana])" + System.lineSeparator() +
                "change [my variable v] by (length of [thelist v])" + System.lineSeparator() +
                "change [my variable v] by ([abs v] of (my variable))" + System.lineSeparator() +
                "change [my variable v] by ((my variable) mod (other variable))" + System.lineSeparator() +
                "change [my variable v] by (round (my variable))" + System.lineSeparator() +
                "change [my variable v] by (pick random (1) to (10))" + System.lineSeparator() +
                "change [my variable v] by ([my variable v] of (Stage v)?)" + System.lineSeparator() +
                "change [my variable v] by ((10)+(my variable))" + System.lineSeparator() +
                "change [my variable v] by (item (1) of [thelist v])" + System.lineSeparator() +
                "change [my variable v] by (item # of [thing] in [thelist v])" + System.lineSeparator() +
                "change [my variable v] by (username)" + System.lineSeparator() +
                "change [my variable v] by (thelist)" + System.lineSeparator() +
                "change [my variable v] by (mouse x)" + System.lineSeparator() +
                "change [my variable v] by (loudness)" + System.lineSeparator() +
                "change [my variable v] by (days since 2000)" + System.lineSeparator() +
                "change [my variable v] by (current (year v))" + System.lineSeparator() +
                "change [my variable v] by (timer)" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(), result);
    }

    @Test
    public void testMoveWithAllOtherblocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/movewithallblocks.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "move (10) steps" + System.lineSeparator() +
                "move (x position) steps" + System.lineSeparator() +
                "move (y position) steps" + System.lineSeparator() +
                "move (direction) steps" + System.lineSeparator() +
                "move (costume [number v]) steps" + System.lineSeparator() +
                "move (backdrop [number v]) steps" + System.lineSeparator() +
                "move (size) steps" + System.lineSeparator() +
                "move ([]+[]) steps" + System.lineSeparator() +
                "move (join [apple ][banana]) steps" + System.lineSeparator() +
                "move ([backdrop # v] of (letter (1) of [apple])?) steps" + System.lineSeparator() +
                "move (loudness) steps" + System.lineSeparator() +
                "move <touching (mouse-pointer v) ?> steps" + System.lineSeparator() +
                "move <[] > (50)> steps" + System.lineSeparator() +
                "move (my variable) steps" + System.lineSeparator() +
                "move ([] mod []) steps" + System.lineSeparator() +
                "move (timer) steps" + System.lineSeparator() +
                "move (days since 2000) steps" + System.lineSeparator() +
                "move (mouse x) steps" + System.lineSeparator() +
                "move <touching color [#e24b5b] ?> steps" + System.lineSeparator() +
                "move (length of (round [])) steps" + System.lineSeparator() +
                "move ([abs v] of []) steps" + System.lineSeparator() +
                "move <[apple] contains [a]?> steps" + System.lineSeparator() +
                "move (distance to (mouse-pointer v)) steps" + System.lineSeparator() +
                "move <color [#d9de7a] is touching [#7efc63] ?> steps" + System.lineSeparator() +
                "move <<> and <>> steps" + System.lineSeparator() +
                "move (answer) steps" + System.lineSeparator() +
                "move (current (year v)) steps" + System.lineSeparator() +
                "move (pick random (1) to (10)) steps" + System.lineSeparator() +
                "move (volume) steps" + System.lineSeparator() +
                "move <key (space v) pressed?> steps" + System.lineSeparator() +
                "move (username) steps" + System.lineSeparator() +
                "move (mouse y) steps" + System.lineSeparator() +
                "move <mouse down?> steps" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(), result);
    }

    @Test
    public void testMultipleCustomBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/multicustomblocks.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "define block1" + System.lineSeparator() +
                "say [Hello!]" + System.lineSeparator() +
                "" + System.lineSeparator() +
                "define block2" + System.lineSeparator() +
                "say [Bye!]" + System.lineSeparator() +
                "" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "block1" + System.lineSeparator() +
                "block2" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testLookBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/lookblocks.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "say [Hello!] for (2) seconds" + System.lineSeparator() +
                "say [Hello!]" + System.lineSeparator() +
                "think [Hmm...] for (2) seconds" + System.lineSeparator() +
                "think [Hmm...]" + System.lineSeparator() +
                "switch costume to (costume2 v)" + System.lineSeparator() +
                "next costume" + System.lineSeparator() +
                "switch backdrop to (backdrop1 v)" + System.lineSeparator() +
                "next backdrop" + System.lineSeparator() +
                "change size by (10)" + System.lineSeparator() +
                "set size to (100) %" + System.lineSeparator() +
                "change [color v] effect by (25)" + System.lineSeparator() +
                "set [color v] effect to (0)" + System.lineSeparator() +
                "clear graphic effects" + System.lineSeparator() +
                "show" + System.lineSeparator() +
                "hide" + System.lineSeparator() +
                "go to [front v] layer" + System.lineSeparator() +
                "go [forward v] (1) layers" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testSoundBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/soundblocks.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "play sound (Meow v) until done" + System.lineSeparator() +
                "start sound (Meow v)" + System.lineSeparator() +
                "stop all sounds" + System.lineSeparator() +
                "change [pitch v] effect by (10)" + System.lineSeparator() +
                "set [pitch v] effect to (100)" + System.lineSeparator() +
                "clear sound effects" + System.lineSeparator() +
                "change volume by (-10)" + System.lineSeparator() +
                "set volume to (100) %" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testMultipleSoundBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/soundblocks2.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when [loudness v] > (10)" + System.lineSeparator() +
                "say (volume)" + System.lineSeparator() +
                "change [pan left/right v] effect by (10)" + System.lineSeparator() +
                "start sound (Meow v)" + System.lineSeparator() +
                "start sound (B Trombone v)" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testSensingBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/sensingblocks.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "ask [What's your name?] and wait" + System.lineSeparator() +
                "set drag mode [draggable v]" + System.lineSeparator() +
                "reset timer" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testVariableBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/variableblocks.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "set [my variable v] to (0)" + System.lineSeparator() +
                "change [my variable v] by (1)" + System.lineSeparator() +
                "show variable [my variable v]" + System.lineSeparator() +
                "hide variable [my variable v]" + System.lineSeparator() +
                "add [thing] to [foo v]" + System.lineSeparator() +
                "delete (1) of [foo v]" + System.lineSeparator() +
                "delete all of [foo v]" + System.lineSeparator() +
                "insert [thing] at (1) of [foo v]" + System.lineSeparator() +
                "replace item (1) of [foo v] with [thing]" + System.lineSeparator() +
                "show list [foo v]" + System.lineSeparator() +
                "hide list [foo v]" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testControlBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/controlblocks.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "wait (1) seconds" + System.lineSeparator() +
                "repeat (10)" + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "if <> then" + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "if <> then" + System.lineSeparator() +
                "else" + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "wait until <>" + System.lineSeparator() +
                "repeat until <>" + System.lineSeparator() +
                "forever " + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "stop [all v]" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
        // TODO: Indentation needs fixing
    }

    @Test
    public void testMessageBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/messageblocks.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when I receive [message1 v]" + System.lineSeparator() +
                "broadcast (message1 v)" + System.lineSeparator() +
                "broadcast (message1 v) and wait" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testCloneBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/cloneblocks.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when I start as a clone " + System.lineSeparator() +
                "create clone of (myself v)" + System.lineSeparator() +
                "create clone of (Fairy v)" + System.lineSeparator() +
                "delete this clone " + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testBackdropBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/backdropblocks.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when backdrop switches to [backdrop1 v]" + System.lineSeparator() +
                "switch backdrop to (backdrop1 v)" + System.lineSeparator() +
                "switch backdrop to (Witch House v)" + System.lineSeparator() +
                "switch backdrop to (next backdrop v)" + System.lineSeparator() +
                "switch backdrop to (previous backdrop v)" + System.lineSeparator() +
                "switch backdrop to (random backdrop v)" + System.lineSeparator() +
                "say ([backdrop # v] of (Stage v)?)" + System.lineSeparator() +
                "say ([backdrop name v] of (Stage v)?)" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testTimerBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/timerblocks.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when [timer v] > (10)" + System.lineSeparator() +
                "say (timer) for (timer) seconds" + System.lineSeparator() +
                "reset timer" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testTimerBlockWithVariable() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/dataflow/timerBlock.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when [timer v] > (my variable)" + System.lineSeparator() +
                "set [my variable v] to (0)" + System.lineSeparator() +
                "" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "set [my variable v] to (60)" + System.lineSeparator() +
                "forever " + System.lineSeparator() +
                "say (timer) for (2) seconds" + System.lineSeparator() +
                "say (my variable) for (2) seconds" + System.lineSeparator() +
                "change [my variable v] by (1)" + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testTimerBlockWithExpression() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/mathExprInTimerBlock.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "set [my variable v] to (0)" + System.lineSeparator() +
                "" + System.lineSeparator() +
                "when [timer v] > ([abs v] of (my variable))" + System.lineSeparator() +
                "say [Hello!]" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testArithmeticBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/arithmeticblocks.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "set [x v] to (0)" + System.lineSeparator() +
                "set [y v] to (0)" + System.lineSeparator() +
                "set [z v] to ((x)+(y))" + System.lineSeparator() +
                "say (z)" + System.lineSeparator() +
                "set [z v] to ((x)-(y))" + System.lineSeparator() +
                "say (z)" + System.lineSeparator() +
                "set [z v] to ((x)*(y))" + System.lineSeparator() +
                "say (z)" + System.lineSeparator() +
                "set [z v] to ((x)/(y))" + System.lineSeparator() +
                "say (z)" + System.lineSeparator() +
                "set [z v] to (round (x))" + System.lineSeparator() +
                "say (z)" + System.lineSeparator() +
                "set [z v] to (pick random (1) to (10))" + System.lineSeparator() +
                "say (z)" + System.lineSeparator() +
                "set [z v] to ((x) mod (y))" + System.lineSeparator() +
                "say (z)" + System.lineSeparator() +
                "set [z v] to ([abs v] of (x))" + System.lineSeparator() +
                "say (z)" + System.lineSeparator() +
                "set [z v] to (((x)-(z))+([abs v] of (x)))" + System.lineSeparator() +
                "say (z)" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testTouchingAllBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/touchingallblocks.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "define block name [number or text] <boolean>" + System.lineSeparator() +
                "wait until <touching (my variable) ?>" + System.lineSeparator() +
                "wait until <touching (x position) ?>" + System.lineSeparator() +
                "wait until <touching (direction) ?>" + System.lineSeparator() +
                "wait until <touching (costume [number v]) ?>" + System.lineSeparator() +
                "wait until <touching (size) ?>" + System.lineSeparator() +
                "wait until <touching (backdrop [number v]) ?>" + System.lineSeparator() +
                "wait until <touching (volume) ?>" + System.lineSeparator() +
                "wait until <touching <touching (mouse-pointer v) ?> ?>" + System.lineSeparator() +
                "wait until <touching <touching color [#c9dae2] ?> ?>" + System.lineSeparator() +
                "wait until <touching <color [#8d6b27] is touching [#805a3c] ?> ?>" + System.lineSeparator() +
                "wait until <touching (distance to (mouse-pointer v)) ?>" + System.lineSeparator() +
                "wait until <touching (answer) ?>" + System.lineSeparator() +
                "wait until <touching <key (space v) pressed?> ?>" + System.lineSeparator() +
                "wait until <touching <mouse down?> ?>" + System.lineSeparator() +
                "wait until <touching (mouse x) ?>" + System.lineSeparator() +
                "wait until <touching (mouse y) ?>" + System.lineSeparator() +
                "wait until <touching (loudness) ?>" + System.lineSeparator() +
                "wait until <touching (timer) ?>" + System.lineSeparator() +
                "wait until <touching ([backdrop # v] of (Stage v)?) ?>" + System.lineSeparator() +
                "wait until <touching (current (year v)) ?>" + System.lineSeparator() +
                "wait until <touching (days since 2000) ?>" + System.lineSeparator() +
                "wait until <touching (username) ?>" + System.lineSeparator() +
                "wait until <touching ([]+[]) ?>" + System.lineSeparator() +
                "wait until <touching (pick random (1) to (10)) ?>" + System.lineSeparator() +
                "wait until <touching <[] > (50)> ?>" + System.lineSeparator() +
                "wait until <touching <<> and <>> ?>" + System.lineSeparator() +
                "wait until <touching (join [apple ][banana]) ?>" + System.lineSeparator() +
                "wait until <touching <[apple] contains [a]?> ?>" + System.lineSeparator() +
                "wait until <touching ([] mod []) ?>" + System.lineSeparator() +
                "wait until <touching ([abs v] of []) ?>" + System.lineSeparator() +
                "wait until <touching (my variable) ?>" + System.lineSeparator() +
                "wait until <touching (listy) ?>" + System.lineSeparator() +
                "wait until <touching (item (1) of [listy v]) ?>" + System.lineSeparator() +
                "wait until <touching (item # of [thing] in [listy v]) ?>" + System.lineSeparator() +
                "wait until <touching (length of [listy v]) ?>" + System.lineSeparator() +
                "wait until <touching <[listy v] contains [thing] ?> ?>" + System.lineSeparator() +
                "wait until <touching (number or text) ?>" + System.lineSeparator() +
                "wait until <touching <boolean> ?>" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testAskAllBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/askallblocks.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "define block name [number or text] <boolean>" + System.lineSeparator() +
                "ask [What's your name?] and wait" + System.lineSeparator() +
                "ask (x position) and wait" + System.lineSeparator() +
                "ask (direction) and wait" + System.lineSeparator() +
                "ask (costume [number v]) and wait" + System.lineSeparator() +
                "ask (backdrop [number v]) and wait" + System.lineSeparator() +
                "ask (size) and wait" + System.lineSeparator() +
                "ask (volume) and wait" + System.lineSeparator() +
                "ask <touching (mouse-pointer v) ?> and wait" + System.lineSeparator() +
                "ask <touching color [#c9dae2] ?> and wait" + System.lineSeparator() +
                "ask <color [#8d6b27] is touching [#805a3c] ?> and wait" + System.lineSeparator() +
                "ask (distance to (mouse-pointer v)) and wait" + System.lineSeparator() +
                "ask (answer) and wait" + System.lineSeparator() +
                "ask <key (space v) pressed?> and wait" + System.lineSeparator() +
                "ask <mouse down?> and wait" + System.lineSeparator() +
                "ask (mouse x) and wait" + System.lineSeparator() +
                "ask (loudness) and wait" + System.lineSeparator() +
                "ask (timer) and wait" + System.lineSeparator() +
                "ask ([backdrop # v] of (Stage v)?) and wait" + System.lineSeparator() +
                "ask (current (year v)) and wait" + System.lineSeparator() +
                "ask (days since 2000) and wait" + System.lineSeparator() +
                "ask (username) and wait" + System.lineSeparator() +
                "ask ([]+[]) and wait" + System.lineSeparator() +
                "ask (pick random (1) to (10)) and wait" + System.lineSeparator() +
                "ask <[] > (50)> and wait" + System.lineSeparator() +
                "ask <<> and <>> and wait" + System.lineSeparator() +
                "ask (join [apple ][banana]) and wait" + System.lineSeparator() +
                "ask ([] mod []) and wait" + System.lineSeparator() +
                "ask ([abs v] of []) and wait" + System.lineSeparator() +
                "ask (my variable) and wait" + System.lineSeparator() +
                "ask (listy) and wait" + System.lineSeparator() +
                "ask (item (1) of [listy v]) and wait" + System.lineSeparator() +
                "ask (item # of [thing] in [listy v]) and wait" + System.lineSeparator() +
                "ask (length of [listy v]) and wait" + System.lineSeparator() +
                "ask <[listy v] contains [thing] ?> and wait" + System.lineSeparator() +
                "ask <boolean> and wait" + System.lineSeparator() +
                "ask (number or text) and wait" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }


    @Test
    public void testBooleanBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/booleanblocks.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "set [x v] to (0)" + System.lineSeparator() +
                "set [y v] to (0)" + System.lineSeparator() +
                "if <(x) > (50)> then" + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "if <(x) < (y)> then" + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "if <[Hello] = (x)> then" + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "if <not <(x) > (y)>> then" + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "if <<(x) > (50)> and <(x) > (y)>> then" + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "if <<(x) > (50)> or <(x) > (y)>> then" + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "say <not <(x) > (y)>>" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testStringBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/stringblocks.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "say (join [apple ][banana])" + System.lineSeparator() +
                "set [x v] to (letter (1) of (x))" + System.lineSeparator() +
                "say (length of (x))" + System.lineSeparator() +
                "ask [What's your name?] and wait" + System.lineSeparator() +
                "if <(answer) contains [a]?> then" + System.lineSeparator() +
                "say [Hello!]" + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testAttributeBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/attributeblocks.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "say (x position)" + System.lineSeparator() +
                "say (y position)" + System.lineSeparator() +
                "say (direction)" + System.lineSeparator() +
                "say (costume [number v])" + System.lineSeparator() +
                "say (answer)" + System.lineSeparator() +
                "say (timer)" + System.lineSeparator() +
                "say (backdrop [number v])" + System.lineSeparator() +
                "say (size)" + System.lineSeparator() +
                "say (volume)" + System.lineSeparator() +
                "say (username)" + System.lineSeparator() +
                "say (loudness)" + System.lineSeparator() +
                "say (distance to (mouse-pointer v))" + System.lineSeparator() +
                "say (current (second v))" + System.lineSeparator() +
                "say (current (year v))" + System.lineSeparator() +
                "say (mouse x)" + System.lineSeparator() +
                "say (mouse y)" + System.lineSeparator() +
                "say (days since 2000)" + System.lineSeparator() +
                "say ([volume v] of (Stage v)?)" + System.lineSeparator() +
                "say ([x position v] of (Prince v)?)" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testSensingConditionBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/sensingconditionblocks.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "wait until <touching (mouse-pointer v) ?>" + System.lineSeparator() +
                "wait until <touching color [#fb9ff6] ?>" + System.lineSeparator() +
                "wait until <color [#19a6d1] is touching [#7daf7d] ?>" + System.lineSeparator() +
                "wait until <key (space v) pressed?>" + System.lineSeparator() +
                "wait until <mouse down?>" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testListAttributeBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/listattributeblocks.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "say (foo)" + System.lineSeparator() +
                "add [thing] to [foo v]" + System.lineSeparator() +
                "say (item (1) of [foo v])" + System.lineSeparator() +
                "say (item # of [thing] in [foo v])" + System.lineSeparator() +
                "say (length of [foo v])" + System.lineSeparator() +
                "wait until <[foo v] contains [thing] ?>" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testCustomBlockNoArgs() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/customblock1.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "define fun_noargs" + System.lineSeparator() +
                "say [Hello!]" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testCustomBlockNumArg() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/customblock2.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "define fun_numarg [num_param]" + System.lineSeparator() +
                "say (num_param)" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testCustomBlockBooleanArg() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/customblock3.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "define fun_numarg <boolean_param>" + System.lineSeparator() +
                "say <boolean_param>" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testCustomBlockMultipleArgs() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/customblock4.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "define fun_numarg <boolean_param> [num_param] label" + System.lineSeparator() +
                "say <boolean_param>" + System.lineSeparator() +
                "say (num_param)" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testCustomBlockCall() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/customblock5.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "define fun_numarg <boolean_param> [num_param] label" + System.lineSeparator() +
                "say <boolean_param>" + System.lineSeparator() +
                "say (num_param)" + System.lineSeparator() +
                "" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "fun_numarg <mouse down?> (username) label" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testPenBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/penblocks.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "erase all" + System.lineSeparator() +
                "stamp" + System.lineSeparator() +
                "pen down" + System.lineSeparator() +
                "pen up" + System.lineSeparator() +
                "set pen color to [#23bb7]" + System.lineSeparator() +
                "change pen (color v) by (10)" + System.lineSeparator() +
                "change pen (brightness v) by (10)" + System.lineSeparator() +
                "set pen (color v) to (50)" + System.lineSeparator() +
                "set pen (saturation v) to (50)" + System.lineSeparator() +
                "change pen size by (1)" + System.lineSeparator() +
                "set pen size to (1)" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testUnconnectedBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/unconnectedblocks.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "repeat (10)" + System.lineSeparator() +
                "say [Hello!] for (2) seconds" + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testMultipleUnconnectedBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/multipleunconnectedblocks.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "show" + System.lineSeparator() +
                "" + System.lineSeparator() +
                "say [Hallo!] for (2) seconds" + System.lineSeparator() +
                "" + System.lineSeparator() +
                "hide" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testStopScriptBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/stopscriptblocks.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "if <key (space v) pressed?> then" + System.lineSeparator() +
                "stop [this script v] " + System.lineSeparator() +
                "else" + System.lineSeparator() +
                "stop [other scripts in sprite v] " + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testSpriteClickedBlock() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/spriteclickedblocks.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when this sprite clicked" + System.lineSeparator() +
                "set [my variable v] to [message 1]" + System.lineSeparator() +
                "broadcast (my variable)" + System.lineSeparator() +
                "broadcast (message1 v)" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testVariableBlocksInSelections() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/variablesinchoiceblocks.json");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(ps);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String result = os.toString();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "go to (my variable)" + System.lineSeparator() +
                "glide (1) secs to (my variable)" + System.lineSeparator() +
                "point towards (my variable)" + System.lineSeparator() +
                "switch costume to (my variable)" + System.lineSeparator() +
                "switch backdrop to (my variable)" + System.lineSeparator() +
                "play sound (my variable) until done" + System.lineSeparator() +
                "start sound (my variable)" + System.lineSeparator() +
                "broadcast (my variable)" + System.lineSeparator() +
                "broadcast (my variable) and wait" + System.lineSeparator() +
                "create clone of (my variable)" + System.lineSeparator() +
                "wait until <key (my variable) pressed?>" + System.lineSeparator() +
                "wait until <touching (my variable) ?>" + System.lineSeparator() +
                "say ([backdrop # v] of (my variable)?)" + System.lineSeparator() +
                "say (distance to (my variable))" + System.lineSeparator() +
                "wait until <touching color (my variable) ?>" + System.lineSeparator() +
                "wait until <color [#ffd824] is touching (my variable) ?>" + System.lineSeparator() +
                "ask (my variable) and wait" + System.lineSeparator() +
                "set pen color to (my variable)" + System.lineSeparator() +
                "change pen (my variable) by (10)" + System.lineSeparator() +
                "set pen (my variable) to (50)" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),result);
    }

    @Test
    public void testComparingLiteralsIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/comparingLiterals.json");
        ComparingLiterals finder = new ComparingLiterals();
        Set<Issue> issues = finder.check(program);

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issues);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "if <<<[] = (50):: #ff0000> and <[] < (50):: #ff0000>> and <[] > (50):: #ff0000>> then // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),output);
    }

    @Test
    public void testMultipleIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/comparingLiterals.json");
        ComparingLiterals finder = new ComparingLiterals();
        Set<Issue> issues = finder.check(program);
        StutteringMovement stuttMovement = new StutteringMovement();

        Issue firstIssue = issues.iterator().next();
        Issue mockIssue = new Issue(stuttMovement, IssueSeverity.HIGH, program, firstIssue.getActor(), firstIssue.getScript(), firstIssue.getCodeLocation(), firstIssue.getCodeMetadata(), new Hint(stuttMovement.getName()));

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(Arrays.asList(firstIssue, mockIssue));
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "if <<<[] = (50):: #ff0000> and <[] < (50)>> and <[] > (50)>> then // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),output);
    }

    @Test
    public void testStutteringMovementIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/stutteringMovement.json");
        StutteringMovement finder = new StutteringMovement();
        Set<Issue> issues = finder.check(program);
        Issue issue = issues.iterator().next();

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        issue.getScript().accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when [space v] key pressed" + System.lineSeparator() +
                "move (10) steps:: #ff0000 // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),output);
    }

    @Test
    public void testEndlessRecursionIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/recursiveProcedure.json");
        EndlessRecursion finder = new EndlessRecursion();
        Set<Issue> issues = finder.check(program);
        Issue issue = issues.iterator().next();

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "define block name [number or text]" + System.lineSeparator() +
                "show variable [my variable v]" + System.lineSeparator() +
                "block name [text]:: #ff0000 // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),output);
    }

    @Test
    public void testCustomBlockWithForeverIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/procedureWithForever.json");
        CustomBlockWithForever finder = new CustomBlockWithForever();
        Set<Issue> issues = finder.check(program);
        Issue issue = issues.iterator().next();

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "block name [reed] <>:: #ff0000 // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "say [Hello!] for (2) seconds" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),output);
    }

    @Test
    public void testCustomBlockWithTerminationIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/procedureWithTermination.json");
        CustomBlockWithTermination finder = new CustomBlockWithTermination();
        Set<Issue> issues = finder.check(program);
        Issue issue = issues.iterator().next();

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "block name:: #ff0000 // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "change size by (10)" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),output);
    }

    @Test
    public void testCallWithoutDefinitionIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/callWithoutDefinition.json");
        CallWithoutDefinition finder = new CallWithoutDefinition();
        Set<Issue> issues = finder.check(program);
        Issue issue = issues.iterator().next();

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "block name [] <>:: #ff0000 // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),output);
    }

    @Test
    public void testAmbiguousParameterNameIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/realAmbiguousParameter.json");
        AmbiguousParameterNameUsed finder = new AmbiguousParameterNameUsed();
        Set<Issue> issues = finder.check(program);
        Issue issue = issues.iterator().next();

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "define block name [number or text] [number or text]:: #ff0000 // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "wait (number or text) seconds" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),output);
    }

    @Test
    public void testAmbiguousSignatureIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/ambiguousProcedureSignature.json");
        AmbiguousCustomBlockSignature finder = new AmbiguousCustomBlockSignature();
        Set<Issue> issues = finder.check(program);
        Issue issue = issues.iterator().next();

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "define AmbiguousParameters [paramTest] [paramTest]:: #ff0000 // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "move (10) steps" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),output);
    }

    @Test
    public void testExpressionAsTouchingOrColorIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/touchingExpressions.json");
        ExpressionAsTouchingOrColor finder = new ExpressionAsTouchingOrColor();
        Set<Issue> issues = finder.check(program);
        Issue issue = issues.iterator().next();

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "<touching color (my variable):: #ff0000 ?> // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),output);
    }

    @Test
    public void testForeverInsideLoopIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/foreverInLoop.json");
        ForeverInsideLoop finder = new ForeverInsideLoop();
        Set<Issue> issues = finder.check(program);
        Issue issue = issues.iterator().next();

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "repeat (10)" + System.lineSeparator() +
                "say [Hello!] for (2) seconds" + System.lineSeparator() +
                "repeat (10)" + System.lineSeparator() +
                "next costume" + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "forever :: #ff0000 // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "point in direction (90)" + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),output);
    }

    @Test
    public void testIllegalParameterRefactorIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/illegalParameterRefactor.json");
        IllegalParameterRefactor finder = new IllegalParameterRefactor();
        Set<Issue> issues = finder.check(program);
        Issue issue = issues.iterator().next();

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "define block name [car] <boolean>" + System.lineSeparator() +
                "if <car:: #ff0000> then // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "move (10) steps" + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),output);
    }

    @Test
    public void testMessageNeverReceivedIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/broadcastSync.json");
        MessageNeverReceived finder = new MessageNeverReceived();
        Set<Issue> issues = finder.check(program);
        Issue issue = issues.iterator().next();

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "broadcast (received v)" + System.lineSeparator() +
                "broadcast (ignored v):: #ff0000 // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),output);
    }

    @Test
    public void testMessageNeverSentIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/messageRec.json");
        MessageNeverSent finder = new MessageNeverSent();
        Set<Issue> issues = finder.check(program);
        Issue issue = issues.iterator().next();

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when I receive [message1 v]:: #ff0000 // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "wait (1) seconds" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),output);
    }

    @Test
    public void testMissingBackdropSwitchIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/missBackdrop.json");
        MissingBackdropSwitch finder = new MissingBackdropSwitch();
        Set<Issue> issues = finder.check(program);
        Issue issue = issues.iterator().next();

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when backdrop switches to [backdrop1 v]:: #ff0000 // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "ask [What's your name?] and wait" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),output);
    }

    @Test
    public void testMissingCloneCallIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/missingCloneCall.json");
        MissingCloneCall finder = new MissingCloneCall();
        Set<Issue> issues = finder.check(program);
        Issue issue = issues.iterator().next();

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when I start as a clone :: #ff0000 // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "wait (1) seconds" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),output);
    }

    @Test
    public void testMissingCloneInitializationIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/missingCloneInitialization.json");
        MissingCloneInitialization finder = new MissingCloneInitialization();
        Set<Issue> issues = finder.check(program);
        Issue issue = issues.iterator().next();

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "create clone of (myself v)" + System.lineSeparator() +
                "create clone of (Anina Dance v):: #ff0000 // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),output);
    }

    @Test
    public void testMissingEraseAllIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/missingEraseAll.json");
        MissingEraseAll finder = new MissingEraseAll();
        Set<Issue> issues = finder.check(program);
        Issue issue = issues.iterator().next();

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "pen down:: #ff0000 // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "pen up" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),output);
    }

    @Test
    public void testMissingInitializationIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/missingVariableInitialization.json");
        MissingInitialization finder = new MissingInitialization();
        Set<Issue> issues = finder.check(program);
        Issue issue = issues.iterator().next();

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "say (meine Variable) for (2) seconds:: #ff0000 // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),output);
    }

    @Test
    public void testMissingLoopSensingIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/nestedMissingLoopSensing.json");
        MissingLoopSensing finder = new MissingLoopSensing();
        Set<Issue> issues = finder.check(program);
        Issue issue = issues.iterator().next();

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "if <<not <touching (mouse-pointer v) ?:: #ff0000>> and <(distance to (mouse-pointer v)) > (50)>> then // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "say [Hallo!] for (2) seconds" + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),output);
    }

    @Test
    public void testMissingPenDownIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/missingPenDown.json");
        MissingPenDown finder = new MissingPenDown();
        Set<Issue> issues = finder.check(program);
        Issue issue = issues.iterator().next();

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "pen up:: #ff0000 // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "set pen color to [#c63f3f]" + System.lineSeparator() +
                "say [Hello!]" + System.lineSeparator() +
                "go to (random position v)" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),output);
    }

    @Test
    public void testMissingPenUpIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/missingPenUp.json");
        MissingPenUp finder = new MissingPenUp();
        Set<Issue> issues = finder.check(program);
        Issue issue = issues.iterator().next();

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "pen down:: #ff0000 // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "say [Hello!]" + System.lineSeparator() +
                "go to (random position v)" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),output);
    }

    @Test
    public void testMissingTerminationConditionIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/missingTermination/missingTermination.json");
        MissingTerminationCondition finder = new MissingTerminationCondition();
        Set<Issue> issues = finder.check(program);
        Issue issue = issues.iterator().next();

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "repeat until <>:: #ff0000 // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "say [Hello!] for (2) seconds" + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),output);
    }

    @Test
    public void testMissingWaitUntilConditionIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/missingWaitUntilCondition.json");
        MissingWaitUntilCondition finder = new MissingWaitUntilCondition();
        Set<Issue> issues = finder.check(program);
        Issue issue = issues.iterator().next();

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "wait until <>:: #ff0000 // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),output);
    }

    @Test
    public void testOrphanedParameterIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/orphanedParameter.json");
        OrphanedParameter finder = new OrphanedParameter();
        Set<Issue> issues = finder.check(program);
        Issue issue = issues.iterator().next();

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "define block name <boolean>" + System.lineSeparator() +
                "move <boolean> steps" + System.lineSeparator() +
                "if <String:: #ff0000> then // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "say [Hello!] for (2) seconds" + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),output);
    }

    @Test
    public void testParameterOutOfScopeIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/parameterOutsideScope.json");
        ParameterOutOfScope finder = new ParameterOutOfScope();
        Set<Issue> issues = finder.check(program);
        Issue issue = issues.iterator().next();

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "turn right <boolean:: #ff0000> degrees // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),output);
    }

    @Test
    public void testRecursiveCloningIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/xPosEqual.json");
        PositionEqualsCheck finder = new PositionEqualsCheck();
        Set<Issue> issues = finder.check(program);
        Issue issue = issues.iterator().next();

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "repeat until <(x position) = (50):: #ff0000> // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),output);
    }

    @Test
    public void testPositionEqualsIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/recursiveCloning.json");
        RecursiveCloning finder = new RecursiveCloning();
        Set<Issue> issues = finder.check(program);
        Issue issue = issues.iterator().next();

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when I start as a clone " + System.lineSeparator() +
                "play sound (Meow v) until done" + System.lineSeparator() +
                "create clone of (myself v):: #ff0000 // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),output);
    }


    @Test
    public void testUnusedVariableIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/smells/unusedVariables.json");
        UnusedVariable finder = new UnusedVariable();
        Set<Issue> issues = finder.check(program);
        Issue issue = issues.iterator().next();

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "(tryvar:: #ff0000) // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(),output);
    }

    @Test
    public void testUnusedListIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/smells/listunused.json");
        UnusedVariable finder = new UnusedVariable();
        Set<Issue> issues = finder.check(program);
        Issue issue = issues.iterator().next();

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "(the list:: #ff0000 :: list) // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(), output);
    }

    @Test
    public void testBuggyListIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/highlightedlist.json");
        MissingInitialization finder = new MissingInitialization();
        Set<Issue> issues = finder.check(program);
        Issue issue = issues.iterator().next();

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when [space v] key pressed" + System.lineSeparator() +
                "delete (1) of [\uD83C\uDF83 Triple click the numbers below. This is your savecode! Press space to close. \uD83C\uDF83 v]:: #ff0000 // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(), output);
    }

    @Test
    public void testShowVariableIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/showvariableannotation.json");
        DeadCode finder = new DeadCode();
        Set<Issue> issues = finder.check(program);

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issues);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "show variable [my variable v]:: #ff0000 // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(), output);
    }


    @Test
    public void testHideVariableIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/hidevariableannotation.json");
        DeadCode finder = new DeadCode();
        Set<Issue> issues = finder.check(program);

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issues);
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "hide variable [my variable v]:: #ff0000 // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(), output);
    }
    // TODO: No working scripts?
    // TODO: SameIdentifierDifferentSprite
}
