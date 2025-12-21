package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class AmbiguousCustomBlockSignatureInjectorTest implements JsonTest {

    @Test
    public void testAmbiguousCustomBlockSignatureInjector() throws IOException, ParsingException {
        // Script with a custom block definition
        String script = """
                define myBlock
                move (10) steps

                when green flag clicked
                myBlock
                """;

        Program program = parseScratchBlocks(script);

        AmbiguousCustomBlockSignatureInjector injector = new AmbiguousCustomBlockSignatureInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertFalse(injected.isEmpty(), "Should inject bug");
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst();

        String result = ScratchBlocksVisitor.of(mutated);

        // We expect the custom block 'myBlock' to appear twice in the definition section
        // One definition is the original: define myBlock \n move (10) steps
        // The duplicate is: define myBlock \n stop [all v]

        Assertions.assertTrue(result.contains("define myBlock" + System.lineSeparator() + "move (10) steps"),
                "Should contain original block");
        Assertions.assertTrue(result.contains("define myBlock" + System.lineSeparator() + "stop [all v]"),
                "Should contain duplicate destructive block");
    }

    @Test
    public void testNoCustomBlockNoInjection() throws IOException, ParsingException {
        // Script without custom block - should not inject
        String script = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        AmbiguousCustomBlockSignatureInjector injector = new AmbiguousCustomBlockSignatureInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertTrue(injected.isEmpty(), "Should not inject bug without custom block");
    }

    @Test
    public void testAmbiguousCustomBlockSignatureInjectorMultipleProcedures() throws IOException, ParsingException {
        // Script with multiple custom blocks
        String script = """
                define unusedBlock

                define usedBlock
                move (10) steps

                when green flag clicked
                usedBlock
                """;

        Program program = parseScratchBlocks(script);

        AmbiguousCustomBlockSignatureInjector injector = new AmbiguousCustomBlockSignatureInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertFalse(injected.isEmpty(), "Should inject bug");
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst();

        String result = ScratchBlocksVisitor.of(mutated);

        // Check for duplicate 'usedBlock'
        // Original: define usedBlock \n move (10) steps
        // Duplicate: define usedBlock \n stop [all v]
        // Unused: define unusedBlock

        Assertions.assertTrue(result.contains("define usedBlock" + System.lineSeparator() + "move (10) steps"),
                "Should contain original usedBlock");
        Assertions.assertTrue(result.contains("define usedBlock" + System.lineSeparator() + "stop [all v]"),
                "Should contain duplicate usedBlock");
        Assertions.assertTrue(result.contains("define unusedBlock"),
                "Should contain unusedBlock");

        // Ensure unusedBlock is NOT duplicated (should appear only once)
        // Counting occurrences of "define unusedBlock"
        int unusedCount = result.split("define unusedBlock", -1).length - 1;
        Assertions.assertEquals(1, unusedCount, "Should not duplicate unusedBlock");
    }
}
