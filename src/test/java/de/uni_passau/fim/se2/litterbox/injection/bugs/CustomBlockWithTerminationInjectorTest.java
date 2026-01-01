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
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.CustomBlockWithTermination;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class CustomBlockWithTerminationInjectorTest implements JsonTest {

    @Test
    public void testCustomBlockWithTerminationInjector() throws IOException, ParsingException {
        // Condition: Custom block called in non-tail position
        String script = """
                define my block
                move (10) steps

                when green flag clicked
                my block
                move (10) steps""";

        Program program = parseScratchBlocks(script);
        CustomBlockWithTerminationInjector injector = new CustomBlockWithTerminationInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertFalse(injected.isEmpty(), "Should have injected at least one bug");
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");

        CustomBlockWithTermination detector = new CustomBlockWithTermination();
        for (InjectionResult result : injected) {
            Program mutated = result.mutatedProgram();
            Set<Issue> issues = detector.check(mutated);
            Assertions.assertFalse(issues.isEmpty(), "CustomBlockWithTermination detector should find the injected bug in all injected programs");
        }
    }

    @Test
    public void testStopAllInsertedInProcedure() throws IOException, ParsingException {
        String script = """
                define myProc
                move (10) steps

                when green flag clicked
                myProc
                say [hello]""";

        Program program = parseScratchBlocks(script);
        CustomBlockWithTerminationInjector injector = new CustomBlockWithTerminationInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verify AST: Procedure should end with StopAll
        boolean foundStopAllInProc = false;
        for (var actor : mutated.getActorDefinitionList().getDefinitions()) {
            for (ProcedureDefinition proc : actor.getProcedureDefinitionList().getList()) {
                List<Stmt> stmts = proc.getStmtList().getStmts();
                if (!stmts.isEmpty() && stmts.getLast() instanceof StopAll) {
                    foundStopAllInProc = true;
                }
            }
        }
        Assertions.assertTrue(foundStopAllInProc, "StopAll should be inserted at the end of the procedure");
    }

    @Test
    public void testNoProcedureNoInjection() throws IOException, ParsingException {
        // Script without procedures - should not inject
        String script = """
                when green flag clicked
                move (10) steps""";

        Program program = parseScratchBlocks(script);
        CustomBlockWithTerminationInjector injector = new CustomBlockWithTerminationInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertTrue(injected.isEmpty(), "Should not inject without a procedure");
    }

    @Test
    public void testTailCallNoInjection() throws IOException, ParsingException {
        // Custom block is called in tail position - should not inject
        String script = """
                define myProc
                move (10) steps

                when green flag clicked
                say [hello]
                myProc""";

        Program program = parseScratchBlocks(script);
        CustomBlockWithTerminationInjector injector = new CustomBlockWithTerminationInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertTrue(injected.isEmpty(), "Should not inject when custom block is in tail position");
    }
}

