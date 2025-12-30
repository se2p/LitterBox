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
        
        ImmediateDeleteCloneAfterBroadcastFix heuristic = new ImmediateDeleteCloneAfterBroadcastFix(bugLocationBlockId);
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
        
        ImmediateDeleteCloneAfterBroadcastFix heuristic = new ImmediateDeleteCloneAfterBroadcastFix(bugLocationBlockId);
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
        
        ImmediateDeleteCloneAfterBroadcastFix heuristic = new ImmediateDeleteCloneAfterBroadcastFix(bugLocationBlockId);
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
        
        ImmediateDeleteCloneAfterBroadcastFix heuristic = new ImmediateDeleteCloneAfterBroadcastFix(bugLocationBlockId);
        assertFixFound(heuristic, program);
    }
}
