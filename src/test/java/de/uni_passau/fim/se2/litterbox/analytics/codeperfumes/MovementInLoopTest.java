package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class MovementInLoopTest implements JsonTest {

    @Test
    public void testMovementsInLoop() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/solutionpattern/movementInLoop.json");
        MovementInLoop movementInLoop = new MovementInLoop();
        Set<Issue> reports = movementInLoop.check(prog);
        Assertions.assertEquals(2, reports.size());
    }

    @Test
    public void testMovesInLoop() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/solutionpattern/movesInLoop.json");
        MovementInLoop movementInLoop = new MovementInLoop();
        Set<Issue> reports = movementInLoop.check(prog);
        Assertions.assertEquals(2, reports.size());
    }

    @Test
    public void testStutteringMovement() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/bugpattern/stutteringMovement.json");
        MovementInLoop movementInLoop = new MovementInLoop();
        Set<Issue> reports = movementInLoop.check(prog);
        Assertions.assertEquals(0, reports.size());
    }
}
