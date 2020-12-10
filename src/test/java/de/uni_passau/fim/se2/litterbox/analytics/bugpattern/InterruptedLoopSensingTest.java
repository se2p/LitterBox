package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class InterruptedLoopSensingTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program empty = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        InterruptedLoopSensing parameterName = new InterruptedLoopSensing();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testInappropriateHandlerDeleteClone() throws IOException, ParsingException {
        Program illegalParameter = JsonTest.parseProgram("./src/test/fixtures/bugpattern/interruptedLoopSensing.json");
        InterruptedLoopSensing parameterName = new InterruptedLoopSensing();
        Set<Issue> reports = parameterName.check(illegalParameter);
        Assertions.assertEquals(2, reports.size());
        List<Issue> issues= new ArrayList<>(reports);
        Hint hint1 = new Hint(parameterName.getName());
        hint1.setParameter(Hint.THEN_ELSE, IssueTranslator.getInstance().getInfo("if") + " " + IssueTranslator.getInstance().getInfo("then"));
        Hint hint2 = new Hint(parameterName.getName());
        hint2.setParameter(Hint.THEN_ELSE, IssueTranslator.getInstance().getInfo("until"));
        Assertions.assertEquals(hint1.getHintText(),issues.get(0).getHint());
        Assertions.assertEquals(hint2.getHintText(),issues.get(1).getHint());
    }

}
