package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.MultiBlockIssue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.SpriteTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LoopSensingTest implements JsonTest {

    @Test
    public void testSensingInLoop() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/solutionpattern/eventInLoop.json");
        LoopSensing LoopSensing = new LoopSensing();
        Set<Issue> reports = LoopSensing.check(prog);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testTwoEventsInLoop() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/solutionpattern/eventInLoopTwo.json");
        LoopSensing LoopSensing = new LoopSensing();
        Set<Issue> reports = LoopSensing.check(prog);
        Assertions.assertEquals(2, reports.size());
    }

    @Test
    public void testMissingSensingForEvent() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/bugpattern/missingLoopSensingMultiple.json");
        LoopSensing LoopSensing = new LoopSensing();
        Set<Issue> reports = LoopSensing.check(prog);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testMultiBlock() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/solutionpattern/loopSensingMultiBlock.json");
        LoopSensing LoopSensing = new LoopSensing();
        List<Issue> reports = new ArrayList<>(LoopSensing.check(prog));
        Assertions.assertEquals(1, reports.size());
        MultiBlockIssue issue = (MultiBlockIssue) reports.get(0);
        List<ASTNode> nodes = issue.getNodes();
        Assertions.assertTrue(nodes.get(0) instanceof UntilStmt);
        Assertions.assertTrue(nodes.get(1) instanceof IfThenStmt);
        Assertions.assertTrue(nodes.get(2) instanceof SpriteTouchingColor);
        Assertions.assertTrue(((UntilStmt) nodes.get(0)).getBoolExpr() instanceof BiggerThan);
    }
}
