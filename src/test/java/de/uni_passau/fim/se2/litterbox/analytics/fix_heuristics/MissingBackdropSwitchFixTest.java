package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.BackdropSwitchTo;
import org.junit.jupiter.api.Test;

import java.io.IOException;

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
        
        MissingBackdropSwitchFix heuristic = new MissingBackdropSwitchFix(bugLocationBlockId);
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
        
        MissingBackdropSwitchFix heuristic = new MissingBackdropSwitchFix(bugLocationBlockId);
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
        
        MissingBackdropSwitchFix heuristic = new MissingBackdropSwitchFix(bugLocationBlockId);
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testSilentDeletion() throws IOException, ParsingException {
        // Silent: The buggy event itself is removed
        String source = """
                when green flag clicked
                switch backdrop to (backdrop1 v)
                """;

        Program program = parseProgram(source);
        // The block is gone, so heuristic should report nothing
        MissingBackdropSwitchFix heuristic = new MissingBackdropSwitchFix("non-existent-id");
        assertNoFixFound(heuristic, program);
    }
}
