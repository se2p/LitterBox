package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class FloatingPointComparisonFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFoundOutsideCondition() throws IOException, ParsingException {
        // Fix: Equals block NOT used as a condition (e.g. dropped where string/number expected)
        // Note: In ScratchBlocks text, we can put boolean reporters in non-boolean slots.
        // "say <(mouse x) = (mouse y)>"
        String source = """
                when green flag clicked
                say <(mouse x) = (mouse y)>
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, Equals.class);
        
        FloatingPointComparisonFix heuristic = new FloatingPointComparisonFix(bugLocationBlockId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFoundSafeOperands() throws IOException, ParsingException {
        // Fix: Equals block inside condition, but operands are safe (e.g., strings)
        String source = """
                when green flag clicked
                if <(username) = [some user]> then
                end
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, Equals.class);
        
        FloatingPointComparisonFix heuristic = new FloatingPointComparisonFix(bugLocationBlockId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFixUnsafeInsideCondition() throws IOException, ParsingException {
        // No Fix: Equals block inside condition check (if/repeat until) AND operands are unsafe (e.g., mouse x)
        String source = """
                when green flag clicked
                if <(mouse x) = (mouse y)> then
                end
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, Equals.class);
        
        FloatingPointComparisonFix heuristic = new FloatingPointComparisonFix(bugLocationBlockId);
        assertNoFixFound(heuristic, program);
    }
}
