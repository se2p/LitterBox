package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class MissingEraseAllTest {
    private static Program empty;
    private static Program eraseOtherSprite;
    private static Program missingEraseAll;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/bugpattern/eraseOtherSprite.json");
        eraseOtherSprite = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/missingEraseAll.json");
        missingEraseAll = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testEmptyProgram() {
        MissingEraseAll parameterName = new  MissingEraseAll();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertTrue(reports.isEmpty());
    }

    @Test
    public void testEraseInOtherSprite() {
        MissingEraseAll parameterName = new  MissingEraseAll();
        Set<Issue> reports = parameterName.check(eraseOtherSprite);
        Assertions.assertTrue(reports.isEmpty());
    }

    @Test
    public void testMissingEraseAll() {
        MissingEraseAll parameterName = new  MissingEraseAll();
        Set<Issue> reports = parameterName.check(missingEraseAll);
        Assertions.assertEquals(1, reports.size());
    }
}
