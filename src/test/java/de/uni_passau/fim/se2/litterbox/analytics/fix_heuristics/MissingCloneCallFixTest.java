package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingCloneCall;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.bugs.MissingCloneCallInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MissingCloneCallFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFoundSpecificSprite() throws IOException, ParsingException {
        // Fix: 'create clone of (Sprite1 v)' is called
        String source = """
                when I start as a clone
                move (10) steps
                
                when green flag clicked
                create clone of (Sprite1 v)
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, StartedAsClone.class);
        
        MissingCloneCallFix heuristic = new MissingCloneCallFix(bugLocationBlockId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFoundMyselfDropdown() throws IOException, ParsingException {
        // Fix: 'create clone of (myself v)' is called
        String source = """
                when I start as a clone
                move (10) steps
                
                when green flag clicked
                create clone of (myself v)
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, StartedAsClone.class);
        
        MissingCloneCallFix heuristic = new MissingCloneCallFix(bugLocationBlockId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFixNoClone() throws IOException, ParsingException {
        // No Fix: No cloning is happening
        String source = """
                when I start as a clone
                move (10) steps
                
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, StartedAsClone.class);
        
        MissingCloneCallFix heuristic = new MissingCloneCallFix(bugLocationBlockId);
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_BlockDeleted() throws IOException, ParsingException {
        // Fix: The buggy block (event) was deleted
        String source = """
                when green flag clicked
                create clone of (myself v)
                """;

        Program program = parseProgram(source);
        // The block is gone, so heuristic should report fix (deletion)
        MissingCloneCallFix heuristic = new MissingCloneCallFix("non-existent-id");
        assertFixFound(heuristic, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    @Test
    public void testInjector_CloneCallRemoved() throws IOException, ParsingException {
        // Fixed: Create clone IS called
        String fixedCode = """
                when I start as a clone
                move (10) steps
                
                when green flag clicked
                create clone of (myself v)
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original program has no bug
        MissingCloneCall bugFinder = new MissingCloneCall();
        Set<Issue> originalIssues = bugFinder.check(fixedProgram);
        Assertions.assertTrue(originalIssues.isEmpty(),
                "Original (fixed) program should have no MissingCloneCall issue");

        // Inject bug: Removes 'create clone of'
        MissingCloneCallInjector injector = new MissingCloneCallInjector();
        List<InjectionResult> injected = injector.inject(fixedProgram);

        Assertions.assertFalse(injected.isEmpty(), "Injector should create at least one bug");

        // Verify the mutated program has bugs
        Program mutatedProgram = injected.getFirst().mutatedProgram();
        Set<Issue> bugIssues = bugFinder.check(mutatedProgram);
        Assertions.assertFalse(bugIssues.isEmpty(),
                "Bug finder should detect MissingCloneCall in mutated program");

        // Get the StartedAsClone block ID from the ORIGINAL program
        String bugId = findBlockId(fixedProgram, StartedAsClone.class);
        Assertions.assertNotNull(bugId, "Should find StartedAsClone block in original program");

        // The fix heuristic should detect this is fixed in the original program
        MissingCloneCallFix fixHeuristic = new MissingCloneCallFix(bugId);
        assertFixFound(fixHeuristic, fixedProgram);
    }
}
