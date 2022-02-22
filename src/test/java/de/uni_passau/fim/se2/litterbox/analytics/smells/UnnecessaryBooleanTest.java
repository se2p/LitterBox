package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class UnnecessaryBooleanTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new UnnecessaryBoolean(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testComparisonToTrue() throws IOException, ParsingException {
        assertThatFinderReports(1, new UnnecessaryBoolean(), "./src/test/fixtures/smells/compareBooleanToTrue.json");
    }

    @Test
    public void testComparisonToOne() throws IOException, ParsingException {
        assertThatFinderReports(1, new UnnecessaryBoolean(), "./src/test/fixtures/smells/compareBooleanToOne.json");
    }

    @Test
    public void testComparisonToFalse() throws IOException, ParsingException {
        assertThatFinderReports(1, new UnnecessaryBoolean(), "./src/test/fixtures/smells/compareBooleanToFalse.json");
    }

    @Test
    public void testComparisonToZero() throws IOException, ParsingException {
        assertThatFinderReports(1, new UnnecessaryBoolean(), "./src/test/fixtures/smells/compareBooleanToZero.json");
    }
}
