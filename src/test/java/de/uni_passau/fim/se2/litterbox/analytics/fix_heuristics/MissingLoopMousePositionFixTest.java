package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.MouseX;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class MissingLoopMousePositionFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound() throws IOException, ParsingException {
        // Fix: MouseX inside a loop
        String source = """
                when green flag clicked
                forever
                    move (mouse x) steps
                end
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, MouseX.class);
        
        MissingLoopMousePositionFix heuristic = new MissingLoopMousePositionFix(bugLocationBlockId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFix() throws IOException, ParsingException {
        // No Fix: MouseX outside a loop
        String source = """
                when green flag clicked
                move (mouse x) steps
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, MouseX.class);
        
        MissingLoopMousePositionFix heuristic = new MissingLoopMousePositionFix(bugLocationBlockId);
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testSilentOnDeletion() throws IOException, ParsingException {
        // Silent: Buggy statement deleted
        String source = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(source);
        
        MissingLoopMousePositionFix heuristic = new MissingLoopMousePositionFix("non_existent_id");
        assertNoFixFound(heuristic, program);
    }
}
