package de.uni_passau.fim.se2.litterbox.export.pystage;

import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class PyStageEqualsTest {

    @Test
    public void testEqualsTypeCoercion() {
        // Test "5" = 5
        Equals equalsNode = new Equals(
                new StringLiteral("5"),
                new NumberLiteral(5),
                new de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata()
        );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream out = new PrintStream(baos, true, StandardCharsets.UTF_8)) {
            PyStageVisitor visitor = new PyStageVisitor(out);
            equalsNode.accept(visitor);
        }
        
        String output = baos.toString(StandardCharsets.UTF_8);
        
        // Before fix: ("5" == 5.0) which is False in Python
        // Expect: _equals("5", 5.0)
        // Failure assertion:
        assertTrue(output.contains("_equals("), "Output should contain _equals helper call, but was: " + output);
    }
}
