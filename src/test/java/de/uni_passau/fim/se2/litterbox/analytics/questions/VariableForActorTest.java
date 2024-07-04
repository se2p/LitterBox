package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class VariableForActorTest implements JsonTest {

    @Test
    public void testNoVariables() throws IOException, ParsingException {
        assertThatFinderReports(0, new VariableForActor(), "./src/test/fixtures/questions/noVariablesInProgram.json");
    }

    @Test
    public void testLocalAndGlobalVariables() throws IOException, ParsingException {
        assertThatFinderReports(2, new VariableForActor(), "./src/test/fixtures/questions/localAndGlobalVariable.json");
    }

    @Test
    public void twoGlobalVariables() throws IOException, ParsingException {
        assertThatFinderReports(2, new VariableForActor(), "src/test/fixtures/questions/twoVariablesInIfElseStatement.json");
    }
}
