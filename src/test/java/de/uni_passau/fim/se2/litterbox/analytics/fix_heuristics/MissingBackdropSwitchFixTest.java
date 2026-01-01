package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingBackdropSwitch;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.BackdropSwitchTo;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.bugs.MissingBackdropSwitchInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MissingBackdropSwitchFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFoundSwitchBackdrop() throws IOException, ParsingException {
        // Fix: Backdrop is switched to in response to green flag
        String source = """
                when backdrop switches to [backdrop1 v]
                move (10) steps
                
                when green flag clicked
                switch backdrop to (backdrop1 v)
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, BackdropSwitchTo.class);
        
        MissingBackdropSwitchFix heuristic = new MissingBackdropSwitchFix(createTestIssueDTO(bugLocationBlockId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFoundNextBackdrop() throws IOException, ParsingException {
        // Fix: Using 'next backdrop' counts as a potential switch to any backdrop
        String source = """
                when backdrop switches to [backdrop1 v]
                move (10) steps
                
                when green flag clicked
                next backdrop
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, BackdropSwitchTo.class);
        
        MissingBackdropSwitchFix heuristic = new MissingBackdropSwitchFix(createTestIssueDTO(bugLocationBlockId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFixDifferentBackdrop() throws IOException, ParsingException {
        // No Fix: Switching to a different backdrop doesn't fix the issue for backdrop1
        String source = """
                when backdrop switches to [backdrop1 v]
                move (10) steps
                
                when green flag clicked
                switch backdrop to (backdrop2 v)
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, BackdropSwitchTo.class);
        
        MissingBackdropSwitchFix heuristic = new MissingBackdropSwitchFix(createTestIssueDTO(bugLocationBlockId));
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_BlockDeleted() throws IOException, ParsingException {
        // Fix: The buggy block (event) was deleted
        String source = """
                when green flag clicked
                switch backdrop to (backdrop1 v)
                """;

        Program program = parseProgram(source);
        // The block is gone, so heuristic should report fix (deletion)
        MissingBackdropSwitchFix heuristic = new MissingBackdropSwitchFix(createTestIssueDTO("non-existent-id"));
        assertFixFound(heuristic, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    @Test
    public void testInjector_SwitchRemoved() throws IOException, ParsingException {
        // Fixed: Switch backdrop IS called
        String fixedCode = """
                when backdrop switches to [backdrop1 v]
                move (10) steps
                
                when green flag clicked
                switch backdrop to (backdrop1 v)
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original program has no bug
        MissingBackdropSwitch bugFinder = new MissingBackdropSwitch();
        Set<Issue> originalIssues = bugFinder.check(fixedProgram);
        Assertions.assertTrue(originalIssues.isEmpty(),
                "Original (fixed) program should have no MissingBackdropSwitch issue");

        // Inject bug: Removes 'switch backdrop to'
        MissingBackdropSwitchInjector injector = new MissingBackdropSwitchInjector();
        List<InjectionResult> injected = injector.inject(fixedProgram);

        Assertions.assertFalse(injected.isEmpty(), "Injector should create at least one bug");

        // Verify the mutated program has bugs
        Program mutatedProgram = injected.getFirst().mutatedProgram();
        Set<Issue> bugIssues = bugFinder.check(mutatedProgram);
        Assertions.assertFalse(bugIssues.isEmpty(),
                "Bug finder should detect MissingBackdropSwitch in mutated program");

        // Get the BackdropSwitchTo block ID from the ORIGINAL program
        String bugId = findBlockId(fixedProgram, BackdropSwitchTo.class);
        Assertions.assertNotNull(bugId, "Should find BackdropSwitchTo block in original program");

        // The fix heuristic should detect this is fixed in the original program
        MissingBackdropSwitchFix fixHeuristic = new MissingBackdropSwitchFix(createTestIssueDTO(bugId));
        assertFixFound(fixHeuristic, fixedProgram);
    }
}
