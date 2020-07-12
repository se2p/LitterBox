package de.uni_passau.fim.se2.litterbox.ast.visitor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ScratchBlocksVisitorTest {

    private Program getAST(String fileName) throws IOException, ParsingException {
        File file = new File(fileName);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode project = objectMapper.readTree(file);
        Program program = ProgramParser.parseProgram("TestProgram", project);
        return program;
    }

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
                "say (distance to (mouse-pointer v)\n" +
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
                "fun_numarg (username) <mouse down?> label\n" +
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
}
