package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class DefinitionOfProcedureTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new DefinitionOfProcedure(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testOneProcedureDefinition() throws IOException, ParsingException {
        assertThatFinderReports(1, new DefinitionOfProcedure(), "src/test/fixtures/questions/oneProcedureDefinitionAndCall.json");
    }

    @Test
    public void testTwoProcedureDefinitions() throws IOException, ParsingException {
        assertThatFinderReports(2, new DefinitionOfProcedure(), "src/test/fixtures/questions/twoProcedures.json");
    }
}
