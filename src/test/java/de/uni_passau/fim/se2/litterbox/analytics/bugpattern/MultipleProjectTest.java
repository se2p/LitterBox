package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class MultipleProjectTest {


    private static Program boatrace;
    private static Program ballgame;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/bugpattern/boatrace.json");
        boatrace = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/ballgame.json");
        ballgame = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testBoatRace() {
        MissingLoopSensing parameterName = new MissingLoopSensing();
        Set<Issue> reports = parameterName.check(boatrace);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testBallgame() {
        MissingLoopSensing parameterName = new MissingLoopSensing();
        Set<Issue> reports = parameterName.check(ballgame);
        Assertions.assertEquals(2, reports.size());
    }

    @Test
    public void testCombined() {
        MissingLoopSensing parameterName = new MissingLoopSensing();
        Set<Issue> reports = parameterName.check(ballgame);
        Assertions.assertEquals(2, reports.size());
        Set<Issue> reports2 = parameterName.check(boatrace);
        Assertions.assertEquals(0, reports2.size());
    }
}
