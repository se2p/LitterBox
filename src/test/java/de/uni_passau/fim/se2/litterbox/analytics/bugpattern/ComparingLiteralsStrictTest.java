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

public class ComparingLiteralsStrictTest {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static Program empty;
    private static Program deadCompare;
    private static Program normal;

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/comparingLiteralsStrict.json");
        deadCompare = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/comparingLiterals.json");
        normal = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testEmptyProgram() {
        ComparingLiterals parameterName = new ComparingLiterals();
        parameterName.setIgnoreLooseBlocks(true);
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testDeadProgram() {
        ComparingLiterals parameterName = new ComparingLiterals();
        parameterName.setIgnoreLooseBlocks(true);
        Set<Issue> reports = parameterName.check(deadCompare);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testNormalProgram() {
        ComparingLiterals parameterName = new ComparingLiterals();
        parameterName.setIgnoreLooseBlocks(true);
        Set<Issue> reports = parameterName.check(normal);
        Assertions.assertEquals(3, reports.size());
    }
}
