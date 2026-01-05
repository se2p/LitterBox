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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.OrphanedParameter;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.bugs.OrphanedParameterInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Tests for OrphanedParameterFix.
 *
 * <p>Bug: A parameter is used in a procedure but not defined in the procedure signature.
 * <p>Fix: The parameter is now in the procedure signature.
 */
public class OrphanedParameterFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_ParameterNowInSignature() throws IOException, ParsingException {
        // Fixed: Parameter "x" is properly defined in the signature and used
        String code = """
                define myBlock (x)
                say (x)
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, Parameter.class,
                param -> "x".equals(param.getName().getName()));

        OrphanedParameterFix finder = new OrphanedParameterFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    @Test
    public void testNoFixFound_ParameterStillOrphaned() throws IOException, ParsingException {
        // Parameter "x" is used but NOT in signature
        String code = """
                define myBlock
                say (x)
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, Parameter.class,
                param -> "x".equals(param.getName().getName()));

        OrphanedParameterFix finder = new OrphanedParameterFix(createTestIssueDTO(blockId));
        assertNoFixFound(finder, program);
    }

    @Test
    public void testFixFound_ScriptDeleted() throws IOException, ParsingException {
        // Script containing the bug is gone
        String code = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(code);
        String blockId = "non-existent-id";

        OrphanedParameterFix finder = new OrphanedParameterFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    @Test
    public void testInjector_OrphanedParam() throws IOException, ParsingException {
        // Start with a valid program (parameter defined and used)
        String fixedCode = """
                define myBlock (x)
                say (x)
                """;
        Program fixedProgram = parseProgram(fixedCode);

        // Verify clean
        OrphanedParameter bugFinder = new OrphanedParameter();
        Set<Issue> originalIssues = bugFinder.check(fixedProgram);
        Assertions.assertTrue(originalIssues.isEmpty(), "Original program should be clean");

        // Inject bug
        OrphanedParameterInjector injector = new OrphanedParameterInjector();
        List<InjectionResult> results = injector.inject(fixedProgram);
        Assertions.assertFalse(results.isEmpty(), "Injector should produce results");

        // Verify bug in mutated
        Program mutatedProgram = results.getFirst().mutatedProgram();
        Set<Issue> bugIssues = bugFinder.check(mutatedProgram);
        Assertions.assertFalse(bugIssues.isEmpty(), "Mutated program should have bugs");

        // Fix check: The bug is likely a parameter usage that is orphaned.
        // In the original program, that usage was valid (or didn't exist in that form).
        // If the injector modified an existing block to be orphaned, we target that block.
        // If the injector removed the definition, the usage acts as the bug location.
        String bugBlockId = AstNodeUtil.getBlockId(bugIssues.iterator().next().getCodeLocation());

        OrphanedParameterFix fix = new OrphanedParameterFix(createTestIssueDTO(bugBlockId));
        assertFixFound(fix, fixedProgram);
    }
}
