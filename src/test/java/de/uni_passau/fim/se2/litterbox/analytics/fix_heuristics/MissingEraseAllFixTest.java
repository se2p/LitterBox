package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenDownStmt;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class MissingEraseAllFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound() throws IOException, ParsingException {
        // Fix: 'erase all' is present in the actor
        String source = """
                when green flag clicked
                pen down
                erase all
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, PenDownStmt.class);
        
        MissingEraseAllFix heuristic = new MissingEraseAllFix(bugLocationBlockId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFoundClear() throws IOException, ParsingException {
        // Fix: 'clear' (alias for erase all) is present
        String source = """
                when green flag clicked
                pen down
                clear
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, PenDownStmt.class);
        
        MissingEraseAllFix heuristic = new MissingEraseAllFix(bugLocationBlockId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFoundAlternativeScript() throws IOException, ParsingException {
        // Fix: 'erase all' is present in a different script of the same actor
        String source = """
                when green flag clicked
                pen down
                
                when I receive [start v]
                erase all
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, PenDownStmt.class);
        
        MissingEraseAllFix heuristic = new MissingEraseAllFix(bugLocationBlockId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFix() throws IOException, ParsingException {
        // No Fix: No 'erase all' or 'clear'
        String source = """
                when green flag clicked
                pen down
                move (10) steps
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, PenDownStmt.class);
        
        MissingEraseAllFix heuristic = new MissingEraseAllFix(bugLocationBlockId);
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testSilentDeletion() throws IOException, ParsingException {
        // Silent: The buggy pen down is removed
        String source = """
                when green flag clicked
                erase all
                """;

        Program program = parseProgram(source);
        MissingEraseAllFix heuristic = new MissingEraseAllFix("non-existent-id");
        assertNoFixFound(heuristic, program);
    }
}
