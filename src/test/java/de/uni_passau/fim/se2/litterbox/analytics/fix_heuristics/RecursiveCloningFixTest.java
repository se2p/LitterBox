package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class RecursiveCloningFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound() throws IOException, ParsingException {
        // Fix: Script has "create clone of myself" AND "delete this clone"
        String source = """
                when I start as a clone
                create clone of (myself v)
                delete this clone
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, CreateCloneOf.class);
        
        RecursiveCloningFix heuristic = new RecursiveCloningFix(createTestIssueDTO(bugLocationBlockId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFix() throws IOException, ParsingException {
        // No Fix: Missing "delete this clone"
        String source = """
                when I start as a clone
                create clone of (myself v)
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, CreateCloneOf.class);
        
        RecursiveCloningFix heuristic = new RecursiveCloningFix(createTestIssueDTO(bugLocationBlockId));
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_BlockDeleted() throws IOException, ParsingException {
        // Silent: Buggy block deleted
        String source = """
                when I start as a clone
                delete this clone
                """;

        Program program = parseProgram(source);
        // bugLocationBlockId is gone
        
        RecursiveCloningFix heuristic = new RecursiveCloningFix(createTestIssueDTO("non_existent_id"));
        assertFixFound(heuristic, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    @Test
    public void testInjector_RecursiveClone() throws IOException, ParsingException {
        // Start with a valid program (no recursive clone loops)
        // Use a script that does NOT delete the clone, but also doesn't create one recursively.
        // The injector looks for scripts starting with "when I start as a clone" that do NOT have "delete this clone".
        String fixedCode = """
                when I start as a clone
                move (10) steps
                """;
        Program fixedProgram = parseProgram(fixedCode);

        // Verify clean
        de.uni_passau.fim.se2.litterbox.analytics.bugs.RecursiveCloning bugFinder = new de.uni_passau.fim.se2.litterbox.analytics.bugs.RecursiveCloning();
        java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> originalIssues = bugFinder.check(fixedProgram);
        org.junit.jupiter.api.Assertions.assertTrue(originalIssues.isEmpty(), "Original program should be clean");

        // Inject bug (adds 'create clone of myself' without delete, or just adds the loop)
        de.uni_passau.fim.se2.litterbox.injection.bugs.RecursiveCloningInjector injector = new de.uni_passau.fim.se2.litterbox.injection.bugs.RecursiveCloningInjector();
        java.util.List<de.uni_passau.fim.se2.litterbox.injection.InjectionResult> results = injector.inject(fixedProgram);
        org.junit.jupiter.api.Assertions.assertFalse(results.isEmpty(), "Injector should produce results");

        // Verify bug in mutated
        Program mutatedProgram = results.get(0).mutatedProgram();
        java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> bugIssues = bugFinder.check(mutatedProgram);
        org.junit.jupiter.api.Assertions.assertFalse(bugIssues.isEmpty(), "Mutated program should have bugs");

        // Fix check: The bug is the 'create clone' call. In the original, it doesn't exist.
        String bugBlockId = de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil.getBlockId(bugIssues.iterator().next().getCodeLocation());
        
        RecursiveCloningFix fix = new RecursiveCloningFix(createTestIssueDTO(bugBlockId));
        assertFixFound(fix, fixedProgram);
    }
}
