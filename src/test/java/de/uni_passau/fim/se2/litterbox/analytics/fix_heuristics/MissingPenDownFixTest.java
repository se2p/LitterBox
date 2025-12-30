package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenUpStmt;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class MissingPenDownFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound() throws IOException, ParsingException {
        // Fix: 'pen down' is present in the actor
        String source = """
                when green flag clicked
                pen up
                pen down
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, PenUpStmt.class);
        
        MissingPenDownFix heuristic = new MissingPenDownFix(bugLocationBlockId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFoundAlternativeScript() throws IOException, ParsingException {
        // Fix: 'pen down' is present in a different script of the same actor
        String source = """
                when green flag clicked
                pen up
                
                when I receive [start v]
                pen down
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, PenUpStmt.class);
        
        MissingPenDownFix heuristic = new MissingPenDownFix(bugLocationBlockId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFix() throws IOException, ParsingException {
        // No Fix: No 'pen down'
        String source = """
                when green flag clicked
                pen up
                move (10) steps
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, PenUpStmt.class);
        
        MissingPenDownFix heuristic = new MissingPenDownFix(bugLocationBlockId);
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testSilentDeletion() throws IOException, ParsingException {
        // Silent: The buggy pen up is removed
        String source = """
                when green flag clicked
                pen down
                """;

        Program program = parseProgram(source);
        MissingPenDownFix heuristic = new MissingPenDownFix("non-existent-id");
        assertNoFixFound(heuristic, program);
    }
}
