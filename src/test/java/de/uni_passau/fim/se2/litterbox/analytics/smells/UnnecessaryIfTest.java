package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class UnnecessaryIfTest implements JsonTest {
    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new UnnecessaryIf(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testUnnecessaryIf() throws IOException, ParsingException {
        assertThatFinderReports(1, new UnnecessaryIf(), "./src/test/fixtures/smells/unnecessaryIf.json");
    }

    @Test
    public void testDoubleIf() throws IOException, ParsingException {
        assertThatFinderReports(0, new UnnecessaryIf(), "./src/test/fixtures/smells/doubleIf.json");
    }

    @Test
    public void testDifferentIf() throws IOException, ParsingException {
        assertThatFinderReports(0, new UnnecessaryIf(), "./src/test/fixtures/smells/separateIfs.json");
    }
}
