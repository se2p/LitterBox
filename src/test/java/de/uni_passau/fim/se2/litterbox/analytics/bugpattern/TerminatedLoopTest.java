package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class TerminatedLoopTest {

    private static ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/emptyProject.json");
        Program empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

        TerminatedLoop terminatedLoop = new TerminatedLoop();
        Set<Issue> reports = terminatedLoop.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testProcedureWithTermination() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/bugpattern/terminatedLoop.json");
        Program terminatedLoop = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

        TerminatedLoop terminatedLoopChecker = new TerminatedLoop();
        Set<Issue> reports = terminatedLoopChecker.check(terminatedLoop);
        Assertions.assertEquals(3, reports.size());
    }

    @Test
    public void testEmptyLoopAndOneIssue() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/bugpattern/emptyLoopAndOneIssue.json");
        Program empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

        TerminatedLoop terminatedLoop = new TerminatedLoop();
        Set<Issue> reports = terminatedLoop.check(empty);
        Assertions.assertEquals(1, reports.size());
    }
}

