package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenDownStmt;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class MissingPenUpFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound() throws IOException, ParsingException {
        // Fix: 'pen up' is present in the actor
        String source = """
                when green flag clicked
                pen down
                pen up
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, PenDownStmt.class);
        
        MissingPenUpFix heuristic = new MissingPenUpFix(bugLocationBlockId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFoundAlternativeScript() throws IOException, ParsingException {
        // Fix: 'pen up' is present in a different script of the same actor
        String source = """
                when green flag clicked
                pen down
                
                when I receive [start v]
                pen up
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, PenDownStmt.class);
        
        MissingPenUpFix heuristic = new MissingPenUpFix(bugLocationBlockId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFix() throws IOException, ParsingException {
        // No Fix: No 'pen up'
        String source = """
                when green flag clicked
                pen down
                move (10) steps
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, PenDownStmt.class);
        
        MissingPenUpFix heuristic = new MissingPenUpFix(bugLocationBlockId);
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testSilentDeletion() throws IOException, ParsingException {
        // Silent: The buggy pen down is removed
        String source = """
                when green flag clicked
                pen up
                """;

        Program program = parseProgram(source);
        MissingPenUpFix heuristic = new MissingPenUpFix("non-existent-id");
        assertNoFixFound(heuristic, program);
    }
}
