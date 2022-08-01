package de.uni_passau.fim.se2.litterbox.analytics.mblock.perfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class MatrixOffScriptTest implements JsonTest {

    @Test
    public void testEmpty() throws ParsingException, IOException {
        assertThatFinderReports(0, new MatrixOffScript(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void motorScreenCodey() throws ParsingException, IOException {
        assertThatFinderReports(1, new MatrixOffScript(), "./src/test/fixtures/mblock/test/turnOffResourcesMotorScreen.json");
    }

    @Test
    public void motorScreenWorkaround() throws ParsingException, IOException {
        assertThatFinderReports(1, new MatrixOffScript(), "./src/test/fixtures/mblock/test/turnOffResourceWorkaround.json");
    }

    @Test
    public void motorScreenWithShutdownBetween() throws ParsingException, IOException {
        assertThatFinderReports(1, new MatrixOffScript(), "./src/test/fixtures/mblock/test/turnOffResourcesInclBlack.json");
    }

    @Test
    public void motorOffScreenNot() throws ParsingException, IOException {
        assertThatFinderReports(0, new MatrixOffScript(), "./src/test/fixtures/mblock/test/turnOffNotComplete.json");
    }
}
