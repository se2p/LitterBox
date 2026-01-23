package de.uni_passau.fim.se2.litterbox.export.goboscript;

import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.Tempo;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GoboscriptTempoTest {

    @Test
    public void testTempoEmit() {
        // say (tempo)
        Tempo tempoNode = new Tempo(new NoBlockMetadata());
        de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString asString = 
            new de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString(tempoNode);
        Say sayNode = new Say(asString, new NoBlockMetadata());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream out = new PrintStream(baos, true, StandardCharsets.UTF_8)) {
            GoboscriptVisitor visitor = new GoboscriptVisitor(out);
            sayNode.accept(visitor);
        }
        
        String output = baos.toString(StandardCharsets.UTF_8);
        
        // Before fix: contains "tempo()"
        // After fix: contains "60" and does NOT contain "tempo()"
        
        // This assertion checks for the DESIRED behavior (fix).
        // To verify reproduction failure first, one would check: assertTrue(output.contains("tempo()"));
        // But since we are moving to fix instantly, I will prepare the final assertions.
        
        // For reproduction step (before fix), running this should FAIL if I assert !contains("tempo()")
        
        assertFalse(output.contains("tempo()"), "Output should NOT contain 'tempo()', but was: " + output);
        assertTrue(output.contains("60"), "Output should contain '60', but was: " + output);
    }
}
