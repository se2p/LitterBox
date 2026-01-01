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
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class IllegalParameterRefactorInjectorTest implements JsonTest {

    @Test
    public void testIllegalParameterRefactorInjector() throws IOException, ParsingException {
        String script = """
                define my block <p1>
                if <p1> then
                    move (10) steps
                end

                when green flag clicked
                my block <key [mouse-pressed?] pressed?>
                """;

        Program program = parseScratchBlocks(script);

        IllegalParameterRefactorInjector injector = new IllegalParameterRefactorInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertFalse(injected.isEmpty(), "Should inject bug");
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verify mutation via string output
        // Note: We can't use the detector on the mutated program because the detector
        // relies on procMap (symbol table) which is populated during parsing and isn't
        // updated when the AST is mutated.
        String result = ScratchBlocksVisitor.of(mutated);

        // The injector changes a boolean parameter <p1> to a string parameter [p1]
        // The original definition has <p1> (boolean), the mutated should have [p1] (string)
        Assertions.assertTrue(result.contains("define my block [p1]"),
                "Boolean parameter should be converted to string. Got:\n" + result);
        // The body still uses <p1> (boolean) which is now mismatched with the definition
        Assertions.assertTrue(result.contains("if <p1> then"),
                "Body should still use boolean reference. Got:\n" + result);
    }
}
