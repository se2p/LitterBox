package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class RecursiveCloningFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound() throws IOException, ParsingException {
        // Fix: Script has "create clone of myself" AND "delete this clone"
        String source = """
                when I start as a clone
                create clone of (myself v)
                delete this clone
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, CreateCloneOf.class);
        
        RecursiveCloningFix heuristic = new RecursiveCloningFix(bugLocationBlockId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFix() throws IOException, ParsingException {
        // No Fix: Missing "delete this clone"
        String source = """
                when I start as a clone
                create clone of (myself v)
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, CreateCloneOf.class);
        
        RecursiveCloningFix heuristic = new RecursiveCloningFix(bugLocationBlockId);
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testSilentOnDeletion() throws IOException, ParsingException {
        // Silent: Buggy block deleted
        String source = """
                when I start as a clone
                delete this clone
                """;

        Program program = parseProgram(source);
        // bugLocationBlockId won't be found, so heuristic should find nothing (or handle null)
        // If we pass a dummy ID that doesn't exist, it should return empty issues (silent)
        
        RecursiveCloningFix heuristic = new RecursiveCloningFix("non_existent_id");
        assertNoFixFound(heuristic, program);
    }
}
