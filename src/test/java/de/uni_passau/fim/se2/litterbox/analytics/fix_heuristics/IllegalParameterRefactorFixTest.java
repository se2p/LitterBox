package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class IllegalParameterRefactorFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound() throws IOException, ParsingException {
        // Fix: Parameter is defined as BOOLEAN (<myParam>) and used in boolean context
        String source = """
                define myProc <myParam>
                if <myParam> then
                end
                """;

        Program program = parseProgram(source);
        // Find the Parameter node usage, not definition. 
        // Usage is inside the If statement.
        String bugLocationBlockId = findBlockId(program, Parameter.class);
        
        IllegalParameterRefactorFix heuristic = new IllegalParameterRefactorFix(bugLocationBlockId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFix() throws IOException, ParsingException {
        // No Fix: Parameter is defined as STRING/NUMBER ((myParam)) but used in boolean context
        // This implies the refactoring (changing to boolean) hasn't happened yet.
        String source = """
                define myProc (myParam)
                if <myParam> then
                end
                """;
        // Note: "<myParam>" usage refers to the parameter. If defined as (myParam), 
        // ScratchBlocks parser might parse usage as <myParam> if it fits the slot, 
        // but the SymbolTable record for "myParam" comes from definition (String).
        
        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, Parameter.class);
        
        IllegalParameterRefactorFix heuristic = new IllegalParameterRefactorFix(bugLocationBlockId);
        assertNoFixFound(heuristic, program);
    }
}
