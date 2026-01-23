package de.uni_passau.fim.se2.litterbox.export.pystage;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.scratchblocks.ScratchBlocksParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PyStageBroadcastTest implements JsonTest {

    @Test
    void testStageBroadcastRegistration() throws IOException, ParsingException {
        String input = """
                when I receive [test_msg v]
                say [hello]
                """;
        Program program = parseProgram(input, "Stage");
        String pyCode = PyStageVisitor.of(program);

        // Check for correct method call
        assertTrue(pyCode.contains("stage.when_i_receive(\"test_msg\", stage_on_message_0)"),
                "Should use stage.when_i_receive for stage broadcast listeners. Generated: " + pyCode);

        // Check absence of manual injection
        assertFalse(pyCode.contains("_core.code_manager.code_blocks"),
                "Should not manually inject into code_blocks");
        assertFalse(pyCode.contains("_core.code_manager.broadcast_blocks"),
                "Should not manually inject into broadcast_blocks");
    }

    @Test
    void testSpriteBroadcastRegistration() throws IOException, ParsingException {
        String input = """
                when I receive [test_msg v]
                say [meow]
                """;
        Program program = parseProgram(input, "Sprite1");
        String pyCode = PyStageVisitor.of(program);

        // Check for correct method call on sprite
        assertTrue(pyCode.contains("Sprite1.when_i_receive_message(\"test_msg\", Sprite1_on_message_0)"),
                "Should use sprite.when_i_receive_message for sprite broadcast listeners. Generated: " + pyCode);
    }

    private Program parseProgram(String code, String targetName) throws IOException, ParsingException {
        Program baseProject = getAST("src/test/fixtures/emptyProject.json");
        ScratchBlocksParser parser = new ScratchBlocksParser();
        return parser.extendProject(baseProject, targetName, code);
    }
}
