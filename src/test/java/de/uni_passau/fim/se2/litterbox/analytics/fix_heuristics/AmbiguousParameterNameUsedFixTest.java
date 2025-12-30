package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class AmbiguousParameterNameUsedFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound() throws IOException, ParsingException {
        // Fix: Custom block parameters are UNIQUE (param1, param2)
        String source = """
                define myProc (param1) (param2)
                """;

        try {
            Program program = parseProgram(source);
            String bugLocationBlockId = findBlockId(program, ProcedureDefinition.class);
            
            AmbiguousParameterNameUsedFix heuristic = new AmbiguousParameterNameUsedFix(bugLocationBlockId);
            assertFixFound(heuristic, program);
        } catch (ParsingException e) {
            // Fallback if ScratchBlocks parser doesn't support 'define' or fails
            // But ideally it should work. If not, we might need manual construction.
            throw e;
        }
    }

    @Test
    public void testNoFix() throws IOException, ParsingException {
        // No Fix: Custom block parameters are DUPLICATE (param1, param1)
        // Note: ScratchBlocks format might not strictly enforce uniqueness during parse, 
        // allowing us to simulate the bug.
        String source = """
                define myProc (param1) (param1)
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, ProcedureDefinition.class);
        
        AmbiguousParameterNameUsedFix heuristic = new AmbiguousParameterNameUsedFix(bugLocationBlockId);
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testNoFixFound_ScriptDeleted() throws IOException, ParsingException {
        // Script containing the bug is gone
        String code = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(code);
        String blockId = "non-existent-id";

        AmbiguousParameterNameUsedFix heuristic = new AmbiguousParameterNameUsedFix(blockId);
        assertNoFixFound(heuristic, program);
    }
}
