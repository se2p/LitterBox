package de.uni_passau.fim.se2.litterbox.analytics.mblock.perfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern.ColorSettingOutOfBounds;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ColorSettingInBoundsTest implements JsonTest {
    @Test
    public void testEmpty() throws ParsingException, IOException {
        assertThatFinderReports(0, new ColorSettingInBounds(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testColorSettingInBounds() throws ParsingException, IOException {
        assertThatFinderReports(4, new ColorSettingInBounds(), "./src/test/fixtures/mblock/test/colorSettingInBounds.json");
    }
}
