package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.RedundantAssignment;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class RedundantAssignmentInjectorTest implements JsonTest {

    @Test
    public void testSetVariableTo() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set [myVar v] to (10)
                """;

        Program program = parseScratchBlocks(script);

        RedundantAssignmentInjector injector = new RedundantAssignmentInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst();

        String result = ScratchBlocksVisitor.of(mutated);

        // Count occurrences of "set [myVar v] to"
        int count = countOccurrences(result, "set [myVar v] to");
        Assertions.assertEquals(2, count, "Should have duplicated SetVariableTo. Got:\n" + result);

        RedundantAssignment detector = new RedundantAssignment();
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find Redundant Assignment");
    }

    @Test
    public void testSetXTo() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set x to (100)
                """;

        Program program = parseScratchBlocks(script);

        RedundantAssignmentInjector injector = new RedundantAssignmentInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject for SetXTo");
        Program mutated = injected.getFirst();

        String result = ScratchBlocksVisitor.of(mutated);

        int count = countOccurrences(result, "set x to");
        Assertions.assertEquals(2, count, "Should have duplicated SetXTo. Got:\n" + result);

        RedundantAssignment detector = new RedundantAssignment();
        Assertions.assertFalse(detector.check(mutated).isEmpty(), "Detector should find bug");
    }

    @Test
    public void testPointInDirection() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                point in direction (90)
                """;

        Program program = parseScratchBlocks(script);

        RedundantAssignmentInjector injector = new RedundantAssignmentInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject for PointInDirection");
        Program mutated = injected.getFirst();

        String result = ScratchBlocksVisitor.of(mutated);

        int count = countOccurrences(result, "point in direction");
        Assertions.assertEquals(2, count, "Should have duplicated PointInDirection. Got:\n" + result);

        RedundantAssignment detector = new RedundantAssignment();
        Assertions.assertFalse(detector.check(mutated).isEmpty(), "Detector should find bug");
    }

    @Test
    public void testSwitchCostumeTo() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                switch costume to (costume1 v)
                """;

        Program program = parseScratchBlocks(script);

        RedundantAssignmentInjector injector = new RedundantAssignmentInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject for SwitchCostumeTo");
        Program mutated = injected.getFirst();

        String result = ScratchBlocksVisitor.of(mutated);

        int count = countOccurrences(result, "switch costume to");
        Assertions.assertEquals(2, count, "Should have duplicated SwitchCostumeTo. Got:\n" + result);

        RedundantAssignment detector = new RedundantAssignment();
        Assertions.assertFalse(detector.check(mutated).isEmpty(), "Detector should find bug");
    }

    @Test
    public void testSetSizeTo() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set size to (100) %
                """;

        Program program = parseScratchBlocks(script);

        RedundantAssignmentInjector injector = new RedundantAssignmentInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject for SetSizeTo");
        Program mutated = injected.getFirst();

        String result = ScratchBlocksVisitor.of(mutated);

        int count = countOccurrences(result, "set size to");
        Assertions.assertEquals(2, count, "Should have duplicated SetSizeTo. Got:\n" + result);

        RedundantAssignment detector = new RedundantAssignment();
        Assertions.assertFalse(detector.check(mutated).isEmpty(), "Detector should find bug");
    }

    private int countOccurrences(String text, String pattern) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(pattern, idx)) != -1) {
            count++;
            idx += pattern.length();
        }
        return count;
    }
}
