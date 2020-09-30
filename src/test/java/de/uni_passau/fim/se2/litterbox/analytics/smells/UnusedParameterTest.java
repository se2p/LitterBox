package de.uni_passau.fim.se2.litterbox.analytics.smells;

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

public class UnusedParameterTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/emptyProject.json");
        Program empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        UnusedParameter parameterName = new UnusedParameter();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testunusedParameters() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/smells/unusedParameter.json");
        Program empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        UnusedParameter parameterName = new UnusedParameter();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(1, reports.size());
    }
}
