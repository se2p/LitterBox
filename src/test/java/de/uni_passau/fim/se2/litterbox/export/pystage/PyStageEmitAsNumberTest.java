package de.uni_passau.fim.se2.litterbox.export.pystage;

import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Add;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Div;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class PyStageEmitAsNumberTest {

    @Test
    public void testNestedArithmeticDoesNotUseToNum() {
        NoBlockMetadata meta = new NoBlockMetadata();
        // (1 + 2)
        Add inner = new Add(new NumberLiteral(1), new NumberLiteral(2), meta);
        // (1 + 2) + 3
        Add outer = new Add(inner, new NumberLiteral(3), meta);

        String result = PyStageVisitor.of(outer);
        
        // Should be cleaner without _to_num
        // Note: NumberLiteral visits as "1.0", "2.0" etc.
        // Expected structure: ((1.0 + 2.0) + 3.0)
        
        assertThat(result).doesNotContain("_to_num");
        assertThat(result).contains("((1 + 2) + 3)");
    }

    @Test
    public void testBooleanUsesToNum() {
        // ChangeVariableBy accepts Expression, so we can pass a boolean (Equals)
        // change [myVar] by <1 = 1>
        
        StrId varName = new StrId("myVar");
        Equals equals = new Equals(new NumberLiteral(1), new NumberLiteral(1), new NoBlockMetadata());
        
        ChangeVariableBy change = new ChangeVariableBy(varName, equals, new NoBlockMetadata());
        
        String result = PyStageVisitor.of(change);
        
        // Expected: ... _to_num(_equals(1, 1)) ...
        // We assert that _to_num IS present for the boolean expression
        
        assertThat(result).contains("_to_num(_equals(1, 1))");
    }

    @Test
    public void testAsNumberWithVariableUsesToNum() {
        // When a Variable is wrapped in AsNumber (as happens in numeric contexts),
        // it should be wrapped with _to_num() to handle string values.
        // This tests: 1 / [myVar] where [myVar] is a variable reference

        NoBlockMetadata meta = new NoBlockMetadata();
        Variable var = new Variable(new StrId("myVar"));
        AsNumber asNum = new AsNumber(var, meta);
        Div div = new Div(new NumberLiteral(1), asNum, meta);

        String result = PyStageVisitor.of(div);

        // The variable should be wrapped with _to_num to handle string values
        assertThat(result).contains("_to_num(self.get_variable(\"myVar\"))");
    }

    @Test
    public void testAsNumberWithNumberLiteralNoToNum() {
        // When AsNumber wraps a NumberLiteral, no _to_num wrapper is needed

        NoBlockMetadata meta = new NoBlockMetadata();
        NumberLiteral num = new NumberLiteral(42);
        AsNumber asNum = new AsNumber(num, meta);

        String result = PyStageVisitor.of(asNum);

        // NumberLiteral should not be wrapped
        assertThat(result).doesNotContain("_to_num");
        assertThat(result).contains("42");
    }
}