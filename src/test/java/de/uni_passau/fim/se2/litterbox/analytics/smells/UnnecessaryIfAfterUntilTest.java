package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.MissingLoopSensing;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UnnecessaryIfAfterUntilTest implements JsonTest {
    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/emptyProject.json");
        UnnecessaryIfAfterUntil parameterName = new UnnecessaryIfAfterUntil();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testIfThenUnnecessary() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/smells/unnecessaryIf.json");
        UnnecessaryIfAfterUntil parameterName = new UnnecessaryIfAfterUntil();
        List<Issue> reports = new ArrayList<>(parameterName.check(empty));
        Assertions.assertEquals(1, reports.size());
        Hint hint = new Hint(parameterName.getName());
        Assertions.assertEquals(hint.getHintText(), reports.get(0).getHint());
    }

    @Test
    public void testCoupling() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/smells/unnecessaryIf.json");
        UnnecessaryIfAfterUntil parameterName = new UnnecessaryIfAfterUntil();
        List<Issue> reportsUnnecessaryIf = new ArrayList<>(parameterName.check(empty));
        Assertions.assertEquals(1, reportsUnnecessaryIf.size());
        MissingLoopSensing mls = new MissingLoopSensing();
        List<Issue> reportsMLS = new ArrayList<>(mls.check(empty));
        Assertions.assertEquals(1, reportsMLS.size());
        Assertions.assertTrue(parameterName.areCouple(reportsUnnecessaryIf.get(0), reportsMLS.get(0)));
    }
}
