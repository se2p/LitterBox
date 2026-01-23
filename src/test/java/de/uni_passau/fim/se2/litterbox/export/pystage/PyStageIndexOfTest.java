package de.uni_passau.fim.se2.litterbox.export.pystage;

import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.IndexOf;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PyStageIndexOfTest {

    @Test
    public void testIndexOfMixedTypes() {
        // item # of (5) in [list]
        // Should use helper, not str() comparison
        
        
        IndexOf indexOf = new IndexOf(
             new NumberLiteral(5),
             new StrId("myList"),
             new NoBlockMetadata()
        );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream out = new PrintStream(baos, true, StandardCharsets.UTF_8)) {
            PyStageVisitor visitor = new PyStageVisitor(out);
            indexOf.accept(visitor);
        }
        
        String output = baos.toString(StandardCharsets.UTF_8);
        
        // Old buggy behavior: contains "if str(x) == str("
        // New expected behavior: contains "_index_of("
        assertTrue(output.contains("_index_of("), "Output should contain _index_of helper call, but was: " + output);
    }
}
