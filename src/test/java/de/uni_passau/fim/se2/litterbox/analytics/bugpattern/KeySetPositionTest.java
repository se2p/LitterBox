package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KeySetPositionTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new KeySetPosition(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testAllVariations() throws IOException, ParsingException {
        assertThatFinderReports(8, new KeySetPosition(), "./src/test/fixtures/bugpattern/keySetPosition.json");
    }

    @Test
    public void testSubsumption() throws IOException, ParsingException {
        Program illegalParameter = JsonTest.parseProgram("./src/test/fixtures/bugpattern/keySetPosition.json");
        KeySetPosition parameterName = new KeySetPosition();
        List<Issue> reports = new ArrayList<>(parameterName.check(illegalParameter));
        Assertions.assertEquals(8, reports.size());
        MissingLoopSensing mls = new MissingLoopSensing();
        List<Issue> reportsMLS = new ArrayList<>(mls.check(illegalParameter));
        Assertions.assertEquals(1, reportsMLS.size());
        Assertions.assertTrue(reports.get(4).isSubsumedBy(reportsMLS.get(0)));
    }

    @Test
    public void testHint() throws IOException, ParsingException {
        Program illegalParameter = JsonTest.parseProgram("./src/test/fixtures/bugpattern/keySetPosition.json");
        KeySetPosition parameterName = new KeySetPosition();
        List<Issue> reports = new ArrayList<>(parameterName.check(illegalParameter));
        Assertions.assertEquals(8, reports.size());
        Hint hint = new Hint(parameterName.getName());
        hint.setParameter("XY","x");
        hint.setParameter(Hint.HINT_KEY,"right arrow");
        Assertions.assertEquals(hint.getHintText(),reports.get(0).getHint());
    }
}
