package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.StartSound;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SwitchCostumeTo;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class MissingResourceFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFoundCostume() throws IOException, ParsingException {
        // Fix: Switching to 'costume1' which exists in Sprite1 in emptyProject.json
        String source = """
                when green flag clicked
                switch costume to (costume1 v)
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, SwitchCostumeTo.class);
        
        MissingResourceFix heuristic = new MissingResourceFix(bugLocationBlockId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFoundSound() throws IOException, ParsingException {
        // Fix: Starting sound 'Meow' which exists in Sprite1 in emptyProject.json
        String source = """
                when green flag clicked
                start sound (Meow v)
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, StartSound.class);
        
        MissingResourceFix heuristic = new MissingResourceFix(bugLocationBlockId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFixNonExistent() throws IOException, ParsingException {
        // No Fix: 'ghost_costume' does not exist
        String source = """
                when green flag clicked
                switch costume to (ghost_costume v)
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, SwitchCostumeTo.class);
        
        MissingResourceFix heuristic = new MissingResourceFix(bugLocationBlockId);
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testSilentDeletion() throws IOException, ParsingException {
        // Silent: The buggy block is removed
        String source = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(source);
        MissingResourceFix heuristic = new MissingResourceFix("non-existent-id");
        assertNoFixFound(heuristic, program);
    }
}
