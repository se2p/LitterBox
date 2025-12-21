package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MissingPenDownInjectorTest implements JsonTest {

    @Test
    public void testMissingPenDownInjector() throws IOException, ParsingException {
        // Script with Pen Down and Pen Up
        String script = """
                when green flag clicked
                pen down
                move (10) steps
                pen up
                """;

        Program program = parseScratchBlocks(script);

        MissingPenDownInjector injector = new MissingPenDownInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst();

        // Verify via scratchblocks output
        String result = ScratchBlocksVisitor.of(mutated);
        Assertions.assertFalse(result.contains("pen down"), "PenDown should be removed");
        Assertions.assertTrue(result.contains("pen up"), "PenUp should remain");
        Assertions.assertTrue(result.contains("move (10) steps"), "Move should remain");

        // Verify Detector finds MissingPenDown
        Set<Issue> issues = injector.getIssueFinder().check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find Missing Pen Down bug");
    }

    @Test
    public void testMultiplePenDownStatements() throws IOException, ParsingException {
        // Script with multiple pen down and pen up statements
        String script = """
                when green flag clicked
                pen down
                move (10) steps
                pen up
                pen down
                move (20) steps
                pen up
                """;

        Program program = parseScratchBlocks(script);

        MissingPenDownInjector injector = new MissingPenDownInjector();
        List<Program> injected = injector.inject(program);

        // Should create one mutation that removes ALL pen down statements
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst();

        // Verify via scratchblocks output - ALL pen down removed, pen up remains
        String result = ScratchBlocksVisitor.of(mutated);
        String expectedScript = """
                when green flag clicked
                move (10) steps
                pen up
                move (20) steps
                pen up
                """;
        Assertions.assertTrue(result.contains(expectedScript.trim()),
                "ALL pen down should be removed, pen up should remain. Got:\n" + result);
        Assertions.assertFalse(result.contains("pen down"), "No pen down should remain");

        // Verify Detector finds MissingPenDown
        Set<Issue> issues = injector.getIssueFinder().check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find Missing Pen Down bug");
    }

    @Test
    public void testNoPenUpNoInjection() throws IOException, ParsingException {
        // Script with only pen down - precondition not met
        String script = """
                when green flag clicked
                pen down
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        MissingPenDownInjector injector = new MissingPenDownInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertTrue(injected.isEmpty(), "Should not inject without pen up");
    }
}
