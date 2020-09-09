package de.uni_passau.fim.se2.litterbox.analytics.smells;

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

class DuplicatedScriptTest {

    private static Program empty;
    private static Program duplicatedScript;
    private static Program duplicatedScriptMinimalDifference;
    private static Program duplicatedScriptDifferentEvent;
    private static Program duplicatedScriptMultipleBlocks;
    private static Program duplicatedScriptOtherSprite;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/smells/duplicatedScript.json");
        duplicatedScript = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/smells/duplicatedScriptMinimalDifference.json");
        duplicatedScriptMinimalDifference = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/smells/duplicatedScriptDifferentEvent.json");
        duplicatedScriptDifferentEvent = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/smells/duplicatedScriptMultipleBlocks.json");
        duplicatedScriptMultipleBlocks = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/smells/duplicatedScriptOtherSprite.json");
        duplicatedScriptOtherSprite = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testEmptyProgram() {
        DeadCode parameterName = new DeadCode();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testProgram() {
        DuplicatedScript finder = new DuplicatedScript();
        Set<Issue> reports = finder.check(duplicatedScript);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testDuplicatedScriptMinimalDifference() {
        DuplicatedScript finder = new DuplicatedScript();
        Set<Issue> reports = finder.check(duplicatedScriptMinimalDifference);
        // x-position and y-position sensing blocks are replaced
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testDuplicatedScriptDifferentEvent() {
        DuplicatedScript finder = new DuplicatedScript();
        Set<Issue> reports = finder.check(duplicatedScriptDifferentEvent);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testDuplicatedScriptMultipleBlocks() {
        DuplicatedScript finder = new DuplicatedScript();
        Set<Issue> reports = finder.check(duplicatedScriptMultipleBlocks);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testDuplicatedScriptOtherSprite() {
        DuplicatedScript finder = new DuplicatedScript();
        Set<Issue> reports = finder.check(duplicatedScriptOtherSprite);
        Assertions.assertEquals(0, reports.size());
    }
}
