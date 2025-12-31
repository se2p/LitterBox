package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class AmbiguousParameterNameUsedFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound() throws IOException, ParsingException {
        // Fix: Custom block parameters are UNIQUE (param1, param2)
        String source = """
                define myProc (param1) (param2)
                """;

        try {
            Program program = parseProgram(source);
            String bugLocationBlockId = findBlockId(program, ProcedureDefinition.class);
            
            AmbiguousParameterNameUsedFix heuristic = new AmbiguousParameterNameUsedFix(bugLocationBlockId);
            assertFixFound(heuristic, program);
        } catch (ParsingException e) {
            // Fallback if ScratchBlocks parser doesn't support 'define' or fails
            // But ideally it should work. If not, we might need manual construction.
            throw e;
        }
    }

    @Test
    public void testNoFix() throws IOException, ParsingException {
        // No Fix: Custom block parameters are DUPLICATE (param1, param1)
        // We use the injector to ensure the AST has duplicates/ambiguity, 
        // as the parser might normalize invalid ScratchBlocks code.
        String fixedCode = """
                define myProc (param1) (param2)
                say (param1)
                say (param2)
                """;
        Program program = parseProgram(fixedCode);
        
        // Inject the bug
        de.uni_passau.fim.se2.litterbox.injection.bugs.AmbiguousParameterNameUsedInjector injector = 
            new de.uni_passau.fim.se2.litterbox.injection.bugs.AmbiguousParameterNameUsedInjector();
        
        Program buggyProgram = injector.inject(program).get(0).mutatedProgram();
        
        String bugLocationBlockId = findBlockId(buggyProgram, ProcedureDefinition.class);
        
        // Ensure the bug actually exists before testing the fix heuristic
        java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> bugs = 
            new de.uni_passau.fim.se2.litterbox.analytics.bugs.AmbiguousParameterNameUsed().check(buggyProgram);
        org.junit.jupiter.api.Assertions.assertFalse(bugs.isEmpty(), "Injection failed - bug not found in buggyProgram");

        AmbiguousParameterNameUsedFix heuristic = new AmbiguousParameterNameUsedFix(bugLocationBlockId);
        assertNoFixFound(heuristic, buggyProgram);
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

        AmbiguousParameterNameUsedFix heuristic = new AmbiguousParameterNameUsedFix(blockId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testInjector_AmbiguousParameter() throws IOException, ParsingException {
        // Start with a valid program (unique parameters) AND USE THEM
        String fixedCode = """
                define myProc (param1) (param2)
                say (param1)
                say (param2)
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Inject the bug: Duplicate parameter names
        de.uni_passau.fim.se2.litterbox.injection.bugs.AmbiguousParameterNameUsedInjector injector =
                new de.uni_passau.fim.se2.litterbox.injection.bugs.AmbiguousParameterNameUsedInjector();

        Program buggyProgram = injector.inject(fixedProgram).get(0).mutatedProgram();

        // Verify the bug is found
        java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> issues =
                new de.uni_passau.fim.se2.litterbox.analytics.bugs.AmbiguousParameterNameUsed().check(buggyProgram);
        org.junit.jupiter.api.Assertions.assertFalse(issues.isEmpty(), "Injector should create at least one bug");

        // Verify the fix heuristic finds the fix (original program)
        de.uni_passau.fim.se2.litterbox.analytics.Issue issue = issues.iterator().next();
        String bugID = de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil.getBlockId(issue.getNormalizedCodeLocation());

        AmbiguousParameterNameUsedFix heuristic = new AmbiguousParameterNameUsedFix(bugID);
        assertFixFound(heuristic, fixedProgram);
    }
}
