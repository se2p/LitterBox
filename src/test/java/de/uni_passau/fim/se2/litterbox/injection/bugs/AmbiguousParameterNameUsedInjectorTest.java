/*
 * Copyright (C) 2019-2026 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.export.scratchblocks.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class AmbiguousParameterNameUsedInjectorTest implements JsonTest {

    @Test
    public void testAmbiguousParameterNameUsedInjector() throws IOException, ParsingException {
        // Custom block with two different parameters
        String script = """
                define my block (param1) (param2)
                say (param1)
                say (param2)
                """;

        Program program = parseScratchBlocks(script);

        AmbiguousParameterNameUsedInjector injector = new AmbiguousParameterNameUsedInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertFalse(injected.isEmpty(), "Should inject bug");
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verify mutation via string output
        // Note: We can't use the detector on the mutated program because the detector
        // relies on procMap (symbol table) which is populated during parsing and isn't
        // updated when the AST is mutated.
        String result = ScratchBlocksVisitor.of(mutated);

        // Check for double usage of param1 in body
        int param1Count = result.split("\\(param1\\)", -1).length - 1;
        // In definition: [param1] [param1] -> 0 occurrences of (param1)
        // In body: say (param1), say (param1) -> 2 occurrences
        // Total 2 occurrences of (param1)
        Assertions.assertTrue(result.contains("define my block [param1] [param1]"), "Definition should be ambiguous");
        Assertions.assertEquals(2, param1Count, "Body references should be updated to param1");
        Assertions.assertFalse(result.contains("(param2)"), "param2 should no longer exist in body or definition");
    }

    @Test
    public void testAmbiguousParameterNameUsedInjectorUnusedParameter() throws IOException, ParsingException {
        // Custom block where the second parameter is NOT used
        String script = """
                define my block (param1) (param2)
                say (param1)
                """;

        Program program = parseScratchBlocks(script);

        AmbiguousParameterNameUsedInjector injector = new AmbiguousParameterNameUsedInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertTrue(injected.isEmpty(), "Should NOT inject bug if second parameter is unused");
    }

    @Test
    public void testAmbiguousParameterNameUsedInjectorUnusedFirstParameter() throws IOException, ParsingException {
        // Custom block where the first parameter is NOT used
        String script = """
                define my block (param1) (param2)
                say (param2)
                """;

        Program program = parseScratchBlocks(script);

        AmbiguousParameterNameUsedInjector injector = new AmbiguousParameterNameUsedInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertTrue(injected.isEmpty(), "Should NOT inject bug if first parameter is unused");
    }

    @Test
    public void testAmbiguousParameterNameUsedInjectorFirstAndThirdUsed() throws IOException, ParsingException {
        // Custom block with 3 parameters where 1st and 3rd are used, but 2nd is NOT used.
        // Should ignore the unused 2nd parameter and rename 3rd to match 1st.
        String script = """
                define my block (param1) (param2) (param3)
                say (param1)
                say (param3)
                """;

        Program program = parseScratchBlocks(script);

        AmbiguousParameterNameUsedInjector injector = new AmbiguousParameterNameUsedInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertFalse(injected.isEmpty(), "Should inject bug when any pair of used parameters exists");
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        String result = ScratchBlocksVisitor.of(mutated);

        Assertions.assertTrue(result.contains("define my block [param1] [param2] [param1]"), "Definition should have ambiguous param1 and param1 (originally param3)");
        Assertions.assertTrue(result.contains("say (param1)"), "Original param1 usage");
        // The original usage of param3 'say (param3)' should be replaced by 'say (param1)'
        // So we expect two 'say (param1)'
        int param1BodyCount = result.split("say \\(param1\\)", -1).length - 1;
        Assertions.assertEquals(2, param1BodyCount, "Both usages should be param1");

        // param2 was not used, so it should not be in the body.
        Assertions.assertFalse(result.contains("say (param2)"), "param2 was not used");
    }
}
