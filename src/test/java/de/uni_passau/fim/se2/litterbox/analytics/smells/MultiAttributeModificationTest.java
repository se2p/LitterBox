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

public class MultiAttributeModificationTest {

    private static Program empty;
    private static Program program;
    private static Program duplicateVariableIncrement;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/smells/multipleAttributeModification.json");
        program = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/smells/multipleValidVariableModifications.json");
        duplicateVariableIncrement = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testEmptyProgram() {
        LongScript parameterName = new LongScript();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testMultiVariableIncrement() {
        MultiAttributeModification finder = new MultiAttributeModification();
        Set<Issue> reports = finder.check(duplicateVariableIncrement);
        // If the two variables modified are different, no warning should be produced
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testMultiAttributeModification() {
        MultiAttributeModification finder = new MultiAttributeModification();
        Set<Issue> reports = finder.check(program);
        Assertions.assertEquals(22, reports.size());
    }
}
