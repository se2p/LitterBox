package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.OrphanedParameter;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class OrphanedParameterInjectorTest implements JsonTest {

    @Test
    public void testOrphanedParameterInjector() throws IOException, ParsingException {
        String script = """
                define myBlock (myParam)
                say (myParam)
                """;

        Program program = parseScratchBlocks(script);

        OrphanedParameterInjector injector = new OrphanedParameterInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst();

        // Verify via scratchblocks - parameter should be removed from definition
        String result = ScratchBlocksVisitor.of(mutated);
        Assertions.assertTrue(result.contains("define myBlock"), "Should still have define block");
        // Parameter should be removed from signature but reference remains in body
        Assertions.assertTrue(result.contains("say (myParam)"), "Parameter reference should remain orphaned");

        OrphanedParameter detector = new OrphanedParameter();
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find Orphaned Parameter");
    }

    @Test
    public void testUnusedParameterNoInjection() throws IOException, ParsingException {
        // Parameter is defined but NOT used in the body
        String script = """
                define myBlock (unusedParam)
                say [hello]
                """;

        Program program = parseScratchBlocks(script);

        OrphanedParameterInjector injector = new OrphanedParameterInjector();
        List<Program> injected = injector.inject(program);

        // Should NOT inject since parameter is not used
        Assertions.assertTrue(injected.isEmpty(),
                "Should NOT inject when parameter is not used in the body");
    }

    @Test
    public void testMultipleParametersRemovesFirstUsed() throws IOException, ParsingException {
        // Two parameters, only second one is used
        String script = """
                define myBlock (param1) (param2)
                say (param2)
                """;

        Program program = parseScratchBlocks(script);

        OrphanedParameterInjector injector = new OrphanedParameterInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst();

        // Verify via scratchblocks - param2 should be removed since it's used
        String result = ScratchBlocksVisitor.of(mutated);
        Assertions.assertTrue(result.contains("param1"), "Unused param1 should remain in signature");
        Assertions.assertTrue(result.contains("say (param2)"), "param2 reference should remain orphaned");

        OrphanedParameter detector = new OrphanedParameter();
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find Orphaned Parameter");
    }
}
