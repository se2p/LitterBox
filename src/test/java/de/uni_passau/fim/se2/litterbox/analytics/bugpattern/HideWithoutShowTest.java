package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class HideWithoutShowTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new HideWithoutShow(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testSound() throws IOException, ParsingException {
        assertThatFinderReports(0, new HideWithoutShow(), "./src/test/fixtures/bugpattern/hideWithoutShowSound.json");
    }

    @Test
    public void testLook() throws IOException, ParsingException {
        assertThatFinderReports(1, new HideWithoutShow(), "./src/test/fixtures/bugpattern/hideWithoutShowLook.json");
    }

    @Test
    public void testCloneShow() throws IOException, ParsingException {
        assertThatFinderReports(0, new HideWithoutShow(), "./src/test/fixtures/bugpattern/hideWithoutShowCloneShow.json");
    }
}
