package de.uni_passau.fim.se2.litterbox.export.pystage;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

public class PyStageAnyKeyTest implements JsonTest {

    @Test
    public void testAnyKeySensing() throws IOException, ParsingException {
        // Test "key [any v] pressed?" block using a JSON fixture
        // to bypass ScratchBlocksParser limitations.
        Program program = getAST("src/test/fixtures/scratchblocks/any_key_sensing.json");
        String result = PyStageVisitor.of(program);
        
        // With fix, it should generate: any(pygame.key.get_pressed())
        assertThat(result).contains("any(pygame.key.get_pressed())");
        assertThat(result).contains("import pygame");
    }


}
