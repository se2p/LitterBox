package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.ColorTouchingColor;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ExpressionAsTouchingOrColorFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFoundColorTouchingColor() throws IOException, ParsingException {
        // Fix: Operands are ColorLiteral
        // "say <color [#FF0000] is touching [#00FF00] ?>"
        String source = """
                when green flag clicked
                say <color [#FF0000] is touching [#00FF00] ?>
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, ColorTouchingColor.class);
        
        ExpressionAsTouchingOrColorFix heuristic = new ExpressionAsTouchingOrColorFix(bugLocationBlockId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFixColorTouchingColor() throws IOException, ParsingException {
        // No Fix: Operands are expressions (e.g., variables), not ColorLiterals
        // "say <color (mouse x) is touching (mouse y) ?>"
        String source = """
                when green flag clicked
                say <color (mouse x) is touching (mouse y) ?>
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, ColorTouchingColor.class);
        
        ExpressionAsTouchingOrColorFix heuristic = new ExpressionAsTouchingOrColorFix(bugLocationBlockId);
        assertNoFixFound(heuristic, program);
    }
}
