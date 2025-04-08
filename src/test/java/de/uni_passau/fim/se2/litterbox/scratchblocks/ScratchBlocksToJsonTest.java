package de.uni_passau.fim.se2.litterbox.scratchblocks;

import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.jsoncreation.ScriptJSONCreator;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class ScratchBlocksToJsonTest {

    private ScriptEntity getScript(String scratchBlocksInput) {
        final ScratchBlocksParser parser = new ScratchBlocksParser();
        if (!scratchBlocksInput.endsWith("\n")) {
            scratchBlocksInput += "\n";
        }
        return parser.parseScript(scratchBlocksInput);
    }

    private Script parseScript(final String scratchBlocksInput) {
        ScriptEntity scriptEntity = getScript(scratchBlocksInput);
        assertInstanceOf(Script.class, scriptEntity);

        return (Script) scriptEntity;
    }

    @Test
    void testSayWithLiteral() throws FileNotFoundException {
        Script script = parseScript("say [ja!]\n");
        String json = ScriptJSONCreator.createScriptJSONString(script, null);
        writeJsonFromString(json, "say");
    }

    @Test
    void testAllStmtsWithoutVariableList() throws FileNotFoundException {
        final String scriptCode = """
                when green flag clicked
                move (10) steps
                turn right (10) degrees
                turn left (10) degrees
                go to (random position v)
                go to (mouse-pointer v)
                go to x: (0) y: (0)
                glide (1) secs to (random position v)
                glide (1) secs to (mouse-pointer v)
                glide (1) secs to x: (0) y: (0)
                point in direction (90)
                point towards (mouse-pointer v)
                change x by (10)
                set x to (0)
                set y to (0)
                change x by (5)
                change y by (5)
                if on edge, bounce
                set rotation style [left-right v]
                set rotation style [don't rotate v]
                set rotation style [all around v]
                say [Hello] for (2) seconds
                say [Hello]
                think [Hello] for (2) seconds
                think [Hello]
                switch costume to (Kostuem2 v)
                next costume
                switch backdrop to (backdrop2 v)
                next backdrop
                change size by (10)
                set size to (100) %
                change [color v] effect by (25)
                change [fisheye v] effect by (25)
                change [whirl v] effect by (25)
                set [color v] effect to (0)
                set [fisheye v] effect to (0)
                set [whirl v] effect to (0)
                clear graphic effects
                show
                hide
                go to [front v] layer
                go to [back v] layer
                go [forward v] (1) layers
                go [backward v] (2) layers
                play sound (Miau v) until done
                play sound (Miau v)
                stop all sounds
                change [pitch v] effect by (10)
                change [pan left/right v] effect by (10)
                set [pitch v] effect to (0)
                set [pan left/right v] effect to (0)
                clear sound effects
                change volume by (-10)
                set volume to (100) %
                broadcast (message1 v)
                broadcast (message2 v) and wait
                wait (1) seconds
                repeat (10)
                forever
                end
                end
                if <> then
                end
                if <> then
                else
                end
                wait until <>
                repeat until <>
                stop [other scripts in sprite v]
                create clone of (myself v)
                ask [question] and wait
                set drag mode [draggable v]
                set drag mode [not draggable v]
                reset timer
                """.stripIndent();
        Script script = parseScript(scriptCode);
        String json = ScriptJSONCreator.createScriptJSONString(script, null);
        writeJsonFromString(json, "stmts");
    }

    private void writeJsonFromString(String jsonString, String name) throws FileNotFoundException {
        try (PrintWriter out = new PrintWriter(name + ".json")) {
            out.println(jsonString);
        }
    }
}
