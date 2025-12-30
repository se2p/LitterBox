package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import org.junit.jupiter.api.Test;

import java.io.IOException;

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
    public void testSilentDeletion() throws IOException, ParsingException {
        // Silent: The buggy event itself is removed
        String source = """
                when green flag clicked
                create clone of (myself v)
                """;

        Program program = parseProgram(source);
        // The block is gone, so heuristic should report nothing
        MissingCloneCallFix heuristic = new MissingCloneCallFix("non-existent-id");
        assertNoFixFound(heuristic, program);
    }
}
