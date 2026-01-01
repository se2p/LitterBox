package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ImmediateDeleteCloneAfterBroadcastFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFoundNoNextStmt() throws IOException, ParsingException {
        // Fix: Broadcast is last statement (no subsequent DeleteClone)
        String source = """
                when I start as a clone
                broadcast (hello v)
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, Broadcast.class);
        
        ImmediateDeleteCloneAfterBroadcastFix heuristic = new ImmediateDeleteCloneAfterBroadcastFix(createTestIssueDTO(bugLocationBlockId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFoundNextStmtNotDeleteClone() throws IOException, ParsingException {
        // Fix: Next statement is MoveSteps (NOT DeleteClone)
        String source = """
                when I start as a clone
                broadcast (hello v)
                move (10) steps
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, Broadcast.class);
        
        ImmediateDeleteCloneAfterBroadcastFix heuristic = new ImmediateDeleteCloneAfterBroadcastFix(createTestIssueDTO(bugLocationBlockId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFixBroadcastFollowedByDeleteClone() throws IOException, ParsingException {
        // No Fix: Next statement *IS* DeleteClone
        String source = """
                when I start as a clone
                broadcast (hello v)
                delete this clone
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, Broadcast.class);
        
        ImmediateDeleteCloneAfterBroadcastFix heuristic = new ImmediateDeleteCloneAfterBroadcastFix(createTestIssueDTO(bugLocationBlockId));
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testFixFoundBroadcastAndWait() throws IOException, ParsingException {
        // Fix: Changed to BroadcastAndWait - using parentheses syntax
        String source = """
                when I start as a clone
                broadcast (hello v) and wait
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, BroadcastAndWait.class);
        
        ImmediateDeleteCloneAfterBroadcastFix heuristic = new ImmediateDeleteCloneAfterBroadcastFix(createTestIssueDTO(bugLocationBlockId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_ScriptDeleted() throws IOException, ParsingException {
        // Script containing the bug is gone
        String code = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(code);
        String blockId = "non-existent-id";

        ImmediateDeleteCloneAfterBroadcastFix heuristic = new ImmediateDeleteCloneAfterBroadcastFix(createTestIssueDTO(blockId));
        assertFixFound(heuristic, program);
    }
    @Test
    public void testNoFix_MultipleNonYieldingStmts() throws IOException, ParsingException {
        // No Fix: Multiple non-yielding statements between Broadcast and DeleteClone
        String source = """
                when I start as a clone
                broadcast (hello v)
                move (10) steps
                change [myVar v] by (1)
                delete this clone
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, Broadcast.class);
        
        ImmediateDeleteCloneAfterBroadcastFix heuristic = new ImmediateDeleteCloneAfterBroadcastFix(createTestIssueDTO(bugLocationBlockId));
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testFixWithWait() throws IOException, ParsingException {
        // Fix: Yielding statement (Wait) between Broadcast and DeleteClone
        String source = """
                when I start as a clone
                broadcast (hello v)
                wait (1) secs
                delete this clone
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, Broadcast.class);
        
        ImmediateDeleteCloneAfterBroadcastFix heuristic = new ImmediateDeleteCloneAfterBroadcastFix(createTestIssueDTO(bugLocationBlockId));
        assertFixFound(heuristic, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    @Test
    public void testInjector_ImmediateDeleteCloneAfterBroadcast() throws IOException, ParsingException {
        // Start with valid program (broadcast NOT followed by delete clone)
        String fixedCode = """
                when green flag clicked
                create clone of (myself v)
                
                when I start as a clone
                broadcast (hello v)
                wait (1) secs
                move (10) steps
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original has no bug
        de.uni_passau.fim.se2.litterbox.analytics.bugs.ImmediateDeleteCloneAfterBroadcast bugFinder =
                new de.uni_passau.fim.se2.litterbox.analytics.bugs.ImmediateDeleteCloneAfterBroadcast();
        java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> originalIssues =
                bugFinder.check(fixedProgram);
        org.junit.jupiter.api.Assertions.assertTrue(originalIssues.isEmpty(),
                "Original program should be clean");

        // Inject bug (adds DeleteClone after Broadcast)
        de.uni_passau.fim.se2.litterbox.injection.bugs.ImmediateDeleteCloneAfterBroadcastInjector injector =
                new de.uni_passau.fim.se2.litterbox.injection.bugs.ImmediateDeleteCloneAfterBroadcastInjector();
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

        // Fix check: Get bug location and verify fix heuristic identifies original as fixed
        String bugBlockId = de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil
                .getBlockId(bugIssues.iterator().next().getCodeLocation());

        ImmediateDeleteCloneAfterBroadcastFix fix = new ImmediateDeleteCloneAfterBroadcastFix(createTestIssueDTO(bugBlockId));
        assertFixFound(fix, fixedProgram);
    }
}
