package de.uni_passau.fim.se2.litterbox.export.pystage;

import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdropAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PyStageBackdropWaitTest {

    @Test
    public void testSwitchBackdropAndWait() {
        // switch backdrop to [backdrop1] and wait
        SwitchBackdropAndWait switchNode = new SwitchBackdropAndWait(
             new WithExpr(new StringLiteral("backdrop1"), new NoBlockMetadata()),
             new NoBlockMetadata()
        );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream out = new PrintStream(baos, true, StandardCharsets.UTF_8)) {
            PyStageVisitor visitor = new PyStageVisitor(out);
            switchNode.accept(visitor);
        }
        
        String output = baos.toString(StandardCharsets.UTF_8);
        
        // Before fix: contains "# Unhandled node"
        // After fix: contains "switch_backdrop_to_and_wait"
        
        assertTrue(output.contains("switch_backdrop_to_and_wait"), "Output should contain switch_backdrop_to_and_wait, but was: " + output);
    }
}
