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
        program.accept(visitor);
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
        program.accept(visitor);
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
        program.accept(visitor);
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
}
