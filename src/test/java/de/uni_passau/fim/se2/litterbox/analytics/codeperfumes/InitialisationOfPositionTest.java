package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class InitialisationOfPositionTest implements JsonTest {

    @Test
    public void testInitLocXandY() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/initLocSetxSetY.json");
        InitialisationOfPosition initialisationOfPosition = new InitialisationOfPosition();
        Set<Issue> reports = initialisationOfPosition.check(prog);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testInitLocInCustomBlock() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/initLocInBlock.json");
        InitialisationOfPosition initialisationOfPosition = new InitialisationOfPosition();
        Set<Issue> reports = initialisationOfPosition.check(prog);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testInitInBoth() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/initLocInBoth.json");
        InitialisationOfPosition initialisationOfPosition = new InitialisationOfPosition();
        Set<Issue> reports = initialisationOfPosition.check(prog);
        Assertions.assertEquals(2, reports.size());
    }
}