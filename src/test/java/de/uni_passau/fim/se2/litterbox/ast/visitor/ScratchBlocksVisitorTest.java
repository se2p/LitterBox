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

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.*;
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
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "move (10) steps\n" +
                "turn right (15) degrees\n" +
                "turn left (15) degrees\n" +
                "go to (random position v)\n" +
                "go to x: (0) y: (0)\n" +
                "glide (1) secs to (random position v)\n" +
                "glide (1) secs to x: (0) y: (0)\n" +
                "point in direction (90)\n" +
                "point towards (mouse-pointer v)\n" +
                "change x by (10)\n" +
                "set x to (0)\n" +
                "change y by (10)\n" +
                "set y to (0)\n" +
                "if on edge, bounce\n" +
                "set rotation style [left-right v]\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "forever \n" +
                "if <touching (edge v) ?> then\n" +
                "say [Hello!] for (2) seconds\n" +
                "end\n" +
                "end\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "define block1\n" +
                "say [Hello!]\n" +
                "\n" +
                "define block2\n" +
                "say [Bye!]\n" +
                "\n" +
                "when green flag clicked\n" +
                "block1\n" +
                "block2\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "say [Hello!] for (2) seconds\n" +
                "say [Hello!]\n" +
                "think [Hmm...] for (2) seconds\n" +
                "think [Hmm...]\n" +
                "switch costume to (costume2 v)\n" +
                "next costume\n" +
                "switch backdrop to (backdrop1 v)\n" +
                "next backdrop\n" +
                "change size by (10)\n" +
                "set size to (100) %\n" +
                "change [color v] effect by (25)\n" +
                "set [color v] effect to (0)\n" +
                "clear graphic effects\n" +
                "show\n" +
                "hide\n" +
                "go to [front v] layer\n" +
                "go [forward v] (1) layers\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "play sound (Meow v) until done\n" +
                "start sound (Meow v)\n" +
                "stop all sounds\n" +
                "change [pitch v] effect by (10)\n" +
                "set [pitch v] effect to (100)\n" +
                "clear sound effects\n" +
                "change volume by (-10)\n" +
                "set volume to (100) %\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "when [loudness v] > (10)\n" +
                "say (volume)\n" +
                "change [pan left/right v] effect by (10)\n" +
                "start sound (Meow v)\n" +
                "start sound (B Trombone v)\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "ask [What's your name?] and wait\n" +
                "set drag mode [draggable v]\n" +
                "reset timer\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "set [my variable v] to (0)\n" +
                "change [my variable v] by (1)\n" +
                "show variable [my variable v]\n" +
                "hide variable [my variable v]\n" +
                "add [thing] to [foo v]\n" +
                "delete (1) of [foo v]\n" +
                "delete all of [foo v]\n" +
                "insert [thing] at (1) of [foo v]\n" +
                "replace item (1) of [foo v] with [thing]\n" +
                "show list [foo v]\n" +
                "hide list [foo v]\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "wait (1) seconds\n" +
                "repeat (10)\n" +
                "end\n" +
                "if <> then\n" +
                "end\n" +
                "if <> then\n" +
                "else\n" +
                "end\n" +
                "wait until <>\n" +
                "repeat until <>\n" +
                "forever \n" +
                "end\n" +
                "end\n" +
                "stop [all v]\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "when I receive [message1 v]\n" +
                "broadcast (message1 v)\n" +
                "broadcast (message1 v) and wait\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "when I start as a clone \n" +
                "create clone of (myself v)\n" +
                "create clone of (Fairy v)\n" +
                "delete this clone \n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "when backdrop switches to [backdrop1 v]\n" +
                "switch backdrop to (backdrop1 v)\n" +
                "switch backdrop to (Witch House v)\n" +
                "switch backdrop to (next backdrop v)\n" +
                "switch backdrop to (previous backdrop v)\n" +
                "switch backdrop to (random backdrop v)\n" +
                "say ([backdrop # v] of (Stage v)?)\n" +
                "say ([backdrop name v] of (Stage v)?)\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "when [timer v] > (10)\n" +
                "say (timer) for (timer) seconds\n" +
                "reset timer\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "when [timer v] > (my variable)\n" +
                "set [my variable v] to (0)\n" +
                "\n" +
                "when green flag clicked\n" +
                "set [my variable v] to (60)\n" +
                "forever \n" +
                "say (timer) for (2) seconds\n" +
                "say (my variable) for (2) seconds\n" +
                "change [my variable v] by (1)\n" +
                "end\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "set [my variable v] to (0)\n" +
                "\n" +
                "when [timer v] > ([abs v] of (my variable))\n" +
                "say [Hello!]\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "set [x v] to (0)\n" +
                "set [y v] to (0)\n" +
                "set [z v] to ((x)+(y))\n" +
                "say (z)\n" +
                "set [z v] to ((x)-(y))\n" +
                "say (z)\n" +
                "set [z v] to ((x)*(y))\n" +
                "say (z)\n" +
                "set [z v] to ((x)/(y))\n" +
                "say (z)\n" +
                "set [z v] to (round (x))\n" +
                "say (z)\n" +
                "set [z v] to (pick random (1) to (10))\n" +
                "say (z)\n" +
                "set [z v] to ((x) mod (y))\n" +
                "say (z)\n" +
                "set [z v] to ([abs v] of (x))\n" +
                "say (z)\n" +
                "set [z v] to (((x)-(z))+([abs v] of (x)))\n" +
                "say (z)\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "set [x v] to (0)\n" +
                "set [y v] to (0)\n" +
                "if <(x) > (50)> then\n" +
                "end\n" +
                "if <(x) < (y)> then\n" +
                "end\n" +
                "if <[Hello] = (x)> then\n" +
                "end\n" +
                "if <not <(x) > (y)>> then\n" +
                "end\n" +
                "if <<(x) > (50)> and <(x) > (y)>> then\n" +
                "end\n" +
                "if <<(x) > (50)> or <(x) > (y)>> then\n" +
                "end\n" +
                "say <not <(x) > (y)>>\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "say (join [apple ][banana])\n" +
                "set [x v] to (letter (1) of (x))\n" +
                "say (length of (x))\n" +
                "ask [What's your name?] and wait\n" +
                "if <(answer) contains [a]?> then\n" +
                "say [Hello!]\n" +
                "end\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "say (x position)\n" +
                "say (y position)\n" +
                "say (direction)\n" +
                "say (costume [number v])\n" +
                "say (answer)\n" +
                "say (timer)\n" +
                "say (backdrop [number v])\n" +
                "say (size)\n" +
                "say (volume)\n" +
                "say (username)\n" +
                "say (loudness)\n" +
                "say (distance to (mouse-pointer v))\n" +
                "say (current (second v)\n" +
                "say (current (year v)\n" +
                "say (mouse x)\n" +
                "say (mouse y)\n" +
                "say (days since 2000)\n" +
                "say ([volume v] of (Stage v)?)\n" +
                "say ([x position v] of (Prince v)?)\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "wait until <touching (mouse-pointer v) ?>\n" +
                "wait until <touching color [#fb9ff6] ?>\n" +
                "wait until <color [#19a6d1] is touching [#7daf7d] ?>\n" +
                "wait until <key (space v) pressed?>\n" +
                "wait until <mouse down?>\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "say (foo)\n" +
                "add [thing] to [foo v]\n" +
                "say (item (1) of [foo v])\n" +
                "say (item # of [thing] in [foo v])\n" +
                "say (length of [foo v])\n" +
                "wait until <[foo v] contains [thing] ?>\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "define fun_noargs\n" +
                "say [Hello!]\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "define fun_numarg [num_param]\n" +
                "say (num_param)\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "define fun_numarg <boolean_param>\n" +
                "say <boolean_param>\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "define fun_numarg <boolean_param> [num_param] label\n" +
                "say <boolean_param>\n" +
                "say (num_param)\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "define fun_numarg <boolean_param> [num_param] label\n" +
                "say <boolean_param>\n" +
                "say (num_param)\n" +
                "\n" +
                "when green flag clicked\n" +
                "fun_numarg <mouse down?> (username) label\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "erase all\n" +
                "stamp\n" +
                "pen down\n" +
                "pen up\n" +
                "set pen color to [#23bb7]\n" +
                "change pen (color v) by (10)\n" +
                "change pen (brightness v) by (10)\n" +
                "set pen (color v) to (50)\n" +
                "set pen (saturation v) to (50)\n" +
                "change pen size by (1)\n" +
                "set pen size to (1)\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "repeat (10)\n" +
                "say [Hello!] for (2) seconds\n" +
                "end\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "show\n" +
                "\n" +
                "say [Hallo!] for (2) seconds\n" +
                "\n" +
                "hide\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "if <key (space v) pressed?> then\n" +
                "stop [this script v] \n" +
                "else\n" +
                "stop [other scripts in sprite v] \n" +
                "end\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "when this sprite clicked\n" +
                "set [my variable v] to [message 1]\n" +
                "broadcast (my variable)\n" +
                "broadcast (message1 v)\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "go to (my variable)\n" +
                "glide (1) secs to (my variable)\n" +
                "point towards (my variable)\n" +
                "switch costume to (my variable)\n" +
                "switch backdrop to (my variable)\n" +
                "play sound (my variable) until done\n" +
                "start sound (my variable)\n" +
                "broadcast (my variable)\n" +
                "broadcast (my variable) and wait\n" +
                "create clone of (my variable)\n" +
                "wait until <key (my variable) pressed?>\n" +
                "wait until <touching (my variable) ?>\n" +
                "say ([backdrop # v] of (my variable)?)\n" +
                "say (distance to (my variable))\n" +
                "wait until <touching color (my variable) ?>\n" +
                "wait until <color [#ffd824] is touching (my variable) ?>\n" +
                "ask (my variable) and wait\n" +
                "set pen color to (my variable)\n" +
                "change pen (my variable) by (10)\n" +
                "set pen (my variable) to (50)\n" +
                "[/scratchblocks]\n", result);
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
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "if <<<[] = (50):: #ff0000> and <[] < (50):: #ff0000>> and <[] > (50):: #ff0000>> then // Comparing Literals\n" +
                "end\n" +
                "[/scratchblocks]\n", output);
    }

    @Test
    public void testMultipleIssueAnnotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/comparingLiterals.json");
        ComparingLiterals finder = new ComparingLiterals();
        Set<Issue> issues = finder.check(program);
        StutteringMovement stuttMovement = new StutteringMovement();

        Issue firstIssue = issues.iterator().next();
        Issue mockIssue = new Issue(stuttMovement, program, firstIssue.getActor(), firstIssue.getScript(), firstIssue.getCodeLocation(), firstIssue.getCodeMetadata());

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(Arrays.asList(firstIssue, mockIssue));
        visitor.begin();
        program.accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "if <<<[] = (50):: #ff0000> and <[] < (50)>> and <[] > (50)>> then // Comparing Literals, Stuttering Movement\n" +
                "end\n" +
                "[/scratchblocks]\n", output);
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
        assertEquals("[scratchblocks]\n" +
                "when [space v] key pressed\n" +
                "move (10) steps:: #ff0000 // Stuttering Movement\n" +
                "[/scratchblocks]\n", output);
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
        assertEquals("[scratchblocks]\n" +
                "define block name [number or text]\n" +
                "show variable [my variable v]\n" +
                "block name [text]:: #ff0000 // Endless Recursion\n" +
                "[/scratchblocks]\n", output);
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
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "block name [reed] <>:: #ff0000 // Custom Block With Forever\n" +
                "say [Hello!] for (2) seconds\n" +
                "[/scratchblocks]\n", output);
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
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "block name:: #ff0000 // Custom Block with Termination\n" +
                "change size by (10)\n" +
                "[/scratchblocks]\n", output);
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
        assertEquals("[scratchblocks]\n" +
                "block name [] <>:: #ff0000 // Call Without Definition\n" +
                "[/scratchblocks]\n", output);
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
        assertEquals("[scratchblocks]\n" +
                "define block name [number or text] [number or text]:: #ff0000 // Ambiguous Parameter Name Used\n" +
                "wait (number or text) seconds\n" +
                "[/scratchblocks]\n", output);
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
        assertEquals("[scratchblocks]\n" +
                "define AmbiguousParameters [paramTest] [paramTest]:: #ff0000 // Ambiguous Custom Block Signature\n" +
                "move (10) steps\n" +
                "[/scratchblocks]\n", output);
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
        assertEquals("[scratchblocks]\n" +
                "<touching color (my variable):: #ff0000 ?> // Expression as touching or color\n" +
                "[/scratchblocks]\n", output);
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
        assertEquals("[scratchblocks]\n" +
                "repeat (10)\n" +
                "say [Hello!] for (2) seconds\n" +
                "repeat (10)\n" +
                "next costume\n" +
                "end\n" +
                "forever :: #ff0000 // Forever inside a Loop\n" +
                "point in direction (90)\n" +
                "end\n" +
                "end\n" +
                "[/scratchblocks]\n", output);
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
        assertEquals("[scratchblocks]\n" +
                "define block name [car] <boolean>\n" +
                "if <car:: #ff0000> then // Illegal Parameter Refactor\n" +
                "move (10) steps\n" +
                "end\n" +
                "[/scratchblocks]\n", output);
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
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "broadcast (received v)\n" +
                "broadcast (ignored v):: #ff0000 // Message Never Received\n" +
                "[/scratchblocks]\n", output);
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
        assertEquals("[scratchblocks]\n" +
                "when I receive [message1 v]:: #ff0000 // Message Never Sent\n" +
                "wait (1) seconds\n" +
                "[/scratchblocks]\n", output);
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
        assertEquals("[scratchblocks]\n" +
                "when backdrop switches to [backdrop1 v]:: #ff0000 // Missing Backdrop Switch\n" +
                "ask [What's your name?] and wait\n" +
                "[/scratchblocks]\n", output);
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
        assertEquals("[scratchblocks]\n" +
                "when I start as a clone :: #ff0000 // Missing Clone Call\n" +
                "wait (1) seconds\n" +
                "[/scratchblocks]\n", output);
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
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "create clone of (myself v)\n" +
                "create clone of (Anina Dance v):: #ff0000 // Missing Clone Initialization\n" +
                "[/scratchblocks]\n", output);
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
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "pen down:: #ff0000 // Missing Erase All\n" +
                "pen up\n" +
                "[/scratchblocks]\n", output);
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
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "say (meine Variable) for (2) seconds:: #ff0000 // Missing Initialization\n" +
                "[/scratchblocks]\n", output);
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
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "if <<not <touching (mouse-pointer v) ?:: #ff0000>> and <(distance to (mouse-pointer v)) > (50)>> then // Missing Loop\n" +
                "say [Hallo!] for (2) seconds\n" +
                "end\n" +
                "[/scratchblocks]\n", output);
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
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "pen up:: #ff0000 // Missing Pen Down\n" +
                "set pen color to [#c63f3f]\n" +
                "say [Hello!]\n" +
                "go to (random position v)\n" +
                "[/scratchblocks]\n", output);
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
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "pen down:: #ff0000 // Missing Pen Up\n" +
                "say [Hello!]\n" +
                "go to (random position v)\n" +
                "[/scratchblocks]\n", output);
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
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "repeat until <>:: #ff0000 // Missing Termination\n" +
                "say [Hello!] for (2) seconds\n" +
                "end\n" +
                "[/scratchblocks]\n", output);
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
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "wait until <>:: #ff0000 // Missing Wait Until Condition\n" +
                "[/scratchblocks]\n", output);
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
        assertEquals("[scratchblocks]\n" +
                "define block name <boolean>\n" +
                "move (boolean) steps\n" +
                "if <String:: #ff0000> then // Orphaned Parameter\n" +
                "say [Hello!] for (2) seconds\n" +
                "end\n" +
                "[/scratchblocks]\n", output);
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
        assertEquals("[scratchblocks]\n" +
                "turn right (boolean:: #ff0000) degrees // Parameter out of Scope\n" +
                "[/scratchblocks]\n", output);
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
        assertEquals("[scratchblocks]\n" +
                "repeat until <(x position) = (50):: #ff0000> // Position Equals Check\n" +
                "end\n" +
                "[/scratchblocks]\n", output);
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
        assertEquals("[scratchblocks]\n" +
                "when I start as a clone \n" +
                "play sound (Meow v) until done\n" +
                "create clone of (myself v):: #ff0000 // Recursive Cloning\n" +
                "[/scratchblocks]\n", output);
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
        assertEquals("[scratchblocks]\n" +
                "(tryvar:: #ff0000) // Unused Variable\n" +
                "[/scratchblocks]\n", output);
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
        assertEquals("[scratchblocks]\n" +
                "(the list:: #ff0000 :: list) // Unused Variable\n" +
                "[/scratchblocks]\n", output);
    }

    // TODO: No working scripts?
    // TODO: SameIdentifierDifferentSprite
}
