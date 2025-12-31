package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.StartSound;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SwitchCostumeTo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.io.IOException;

public class MissingResourceFixTest extends AbstractFixHeuristicTest {

    @Test
    @Disabled("Dependent on pending Jackson deserialization fix")
    public void testFixFoundCostume() throws IOException, ParsingException {
        // Fix: Switching to 'costume1' which exists in Sprite1 in emptyProject.json
        String source = """
                when green flag clicked
                switch costume to (costume1 v)
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, SwitchCostumeTo.class);
        
        MissingResourceFix heuristic = new MissingResourceFix(bugLocationBlockId);
        assertFixFound(heuristic, program);
    }

    @Test
    @Disabled("Dependent on pending Jackson deserialization fix")
    public void testFixFoundSound() throws IOException, ParsingException {
        // Fix: Starting sound 'Meow' which exists in Sprite1 in emptyProject.json
        String source = """
                when green flag clicked
                start sound (Meow v)
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, StartSound.class);
        
        MissingResourceFix heuristic = new MissingResourceFix(bugLocationBlockId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFixNonExistent() throws IOException, ParsingException {
        // No Fix: 'ghost_costume' does not exist
        String source = """
                when green flag clicked
                switch costume to (ghost_costume v)
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, SwitchCostumeTo.class);
        
        MissingResourceFix heuristic = new MissingResourceFix(bugLocationBlockId);
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testSilentDeletion() throws IOException, ParsingException {
        // Silent: The buggy block is removed
        String source = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(source);
        MissingResourceFix heuristic = new MissingResourceFix("non-existent-id");
        assertNoFixFound(heuristic, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    /**
     * Injector-based test for MissingResource.
     *
     * <p>NOTE: This test is disabled due to pending Jackson deserialization fix
     * that affects resource metadata resolution in the ScratchBlocks parser.
     */
    @Test
    @Disabled("Dependent on pending Jackson deserialization fix for resource metadata")
    public void testInjector_MissingResource() throws IOException, ParsingException {
        // Start with valid program (valid costume reference)
        String fixedCode = """
                when green flag clicked
                switch costume to (costume1 v)
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original has no bug
        de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingResource bugFinder =
                new de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingResource();
        java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> originalIssues =
                bugFinder.check(fixedProgram);
        org.junit.jupiter.api.Assertions.assertTrue(originalIssues.isEmpty(),
                "Original program should be clean");

        // Inject bug (replaces valid costume with invalid name)
        de.uni_passau.fim.se2.litterbox.injection.bugs.MissingResourceInjector injector =
                new de.uni_passau.fim.se2.litterbox.injection.bugs.MissingResourceInjector();
        java.util.List<de.uni_passau.fim.se2.litterbox.injection.InjectionResult> results =
                injector.inject(fixedProgram);
        org.junit.jupiter.api.Assertions.assertFalse(results.isEmpty(),
                "Injector should produce results");

        // Verify bug in mutated
        Program mutatedProgram = results.get(0).mutatedProgram();
        java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> bugIssues =
                bugFinder.check(mutatedProgram);
        org.junit.jupiter.api.Assertions.assertFalse(bugIssues.isEmpty(),
                "Mutated program should have bugs");

        // Use existing SwitchCostumeTo ID from original
        String existingBlockId = findBlockId(fixedProgram, SwitchCostumeTo.class);
        MissingResourceFix fix = new MissingResourceFix(existingBlockId);
        assertFixFound(fix, fixedProgram);
    }
}
