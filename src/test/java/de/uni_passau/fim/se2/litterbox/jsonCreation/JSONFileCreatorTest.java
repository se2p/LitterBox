package de.uni_passau.fim.se2.litterbox.jsonCreation;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JSONFileCreatorTest implements JsonTest {

    @Test
    public void testSb3Creation() throws IOException, ParsingException {
        File empty = new File("./src/test/fixtures/emptyProject.sb3");
        Program emptyProg = JsonTest.parseProgram("./src/test/fixtures/emptyProject.sb3");
        JSONFileCreator.writeSb3FromProgram(emptyProg,"./",empty);
        File generated = new File("./emptyProject_annotated.sb3");
        Assertions.assertTrue(generated.exists());
        Files.delete(Path.of(generated.getPath()));
        Assertions.assertFalse(generated.exists());
    }
}
