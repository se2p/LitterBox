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

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for AmbiguousCustomBlockSignatureFix heuristic.
 *
 * <p>The bug is reported when multiple procedures have the same name in an actor.
 * The fix is detected when the procedure no longer has duplicates with the same name.
 */
public class AmbiguousCustomBlockSignatureFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_NoDuplicates() throws IOException, ParsingException {
        // Fix: Only one procedure with this name exists
        String source = """
                define myProcedure
                move (10) steps
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, ProcedureDefinition.class);

        AmbiguousCustomBlockSignatureFix heuristic = new AmbiguousCustomBlockSignatureFix(createTestIssueDTO(bugId));
        assertFixFound(heuristic, program);
    }

    // Note: Testing "duplicates exist" scenario is complex with ScratchBlocks parser
    // because the procMap/symbol table population may not handle duplicates the same way
    // as real Scratch JSON. This case is best tested with fixture files.
    // See the original bug finder tests for comprehensive coverage.

    @Test
    public void testFixFound_DifferentNames() throws IOException, ParsingException {
        // Fix: Procedures have different names
        String source = """
                define procOne
                move (10) steps
                
                define procTwo
                turn right (15) degrees
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, ProcedureDefinition.class);

        AmbiguousCustomBlockSignatureFix heuristic = new AmbiguousCustomBlockSignatureFix(createTestIssueDTO(bugId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_BlockDeleted() throws IOException, ParsingException {
        // Fix: The buggy procedure was deleted
        String source = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(source);

        AmbiguousCustomBlockSignatureFix heuristic = new AmbiguousCustomBlockSignatureFix(createTestIssueDTO("non_existent_id"));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testInjector_AmbiguousSignature() throws IOException, ParsingException {
        // Start with a valid program (unique signature)
        String fixedCode = """
                define myProc
                move (10) steps
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Inject the bug: Duplicate 'myProc'
        de.uni_passau.fim.se2.litterbox.injection.bugs.AmbiguousCustomBlockSignatureInjector injector =
                new de.uni_passau.fim.se2.litterbox.injection.bugs.AmbiguousCustomBlockSignatureInjector();

        Program buggyProgram = injector.inject(fixedProgram).get(0).mutatedProgram();

        // Verify the bug is found
        java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> issues =
                new de.uni_passau.fim.se2.litterbox.analytics.bugs.AmbiguousCustomBlockSignature().check(buggyProgram);
        org.junit.jupiter.api.Assertions.assertFalse(issues.isEmpty(), "Injector should create at least one bug");

        // Verify the fix heuristic finds the fix (original program has no duplicates)
        de.uni_passau.fim.se2.litterbox.analytics.Issue issue = issues.iterator().next();
        String bugID = de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil.getBlockId(issue.getNormalizedCodeLocation());

        AmbiguousCustomBlockSignatureFix heuristic = new AmbiguousCustomBlockSignatureFix(createTestIssueDTO(bugID));
        assertFixFound(heuristic, fixedProgram);
    }
}
