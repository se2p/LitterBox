package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class BackdropSwitchAndEventTest implements JsonTest {


    @Test
    public void testSimpleBackdropSwitchAndEvent() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/backdropEvent.json");
        BackdropSwitchAndEvent backdropSwitchAndEvent = new BackdropSwitchAndEvent();
        Set<Issue> reports = backdropSwitchAndEvent.check(prog);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testWithRandomBackdrop() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/backdropRandom.json");
        BackdropSwitchAndEvent backdropSwitchAndEvent = new BackdropSwitchAndEvent();
        Set<Issue> reports = backdropSwitchAndEvent.check(prog);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testBackdropEventWithoutEventAndOneCorrect() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/backdropEventWithoutSwitch.json");
        BackdropSwitchAndEvent backdropSwitchAndEvent = new BackdropSwitchAndEvent();
        Set<Issue> reports = backdropSwitchAndEvent.check(prog);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testBackdropEventNoSwitch() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/backdropEventNoSwitch.json");
        BackdropSwitchAndEvent backdropSwitchAndEvent = new BackdropSwitchAndEvent();
        Set<Issue> reports = backdropSwitchAndEvent.check(prog);
        Assertions.assertEquals(0, reports.size());
    }
}


