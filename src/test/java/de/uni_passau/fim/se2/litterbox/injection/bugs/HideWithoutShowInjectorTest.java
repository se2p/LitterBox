package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.HideWithoutShow;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Show;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class HideWithoutShowInjectorTest implements JsonTest {

    @Test
    public void testHideWithoutShowInjector() throws IOException, ParsingException {
        // Script with show and hide
        String script = """
                when green flag clicked
                show
                move (10) steps
                wait (1) secs
                hide
                """;

        Program program = parseScratchBlocks(script);

        HideWithoutShowInjector injector = new HideWithoutShowInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertFalse(injected.isEmpty(), "Should inject bug");
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst();

        // Verification: Check detector
        HideWithoutShow detector = new HideWithoutShow();
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find Hide Without Show issue");
    }

    @Test
    public void testShowRemoved() throws IOException, ParsingException {
        // Script with show - verify it's removed after injection
        String script = """
                when green flag clicked
                show
                move (10) steps
                hide
                """;

        Program program = parseScratchBlocks(script);

        // Verify original has Show
        Assertions.assertTrue(hasShow(program), "Original should have Show");

        HideWithoutShowInjector injector = new HideWithoutShowInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst();

        // Verify Show is removed
        Assertions.assertFalse(hasShow(mutated), "Show should be removed after injection");
    }

    @Test
    public void testNoShowNoInjection() throws IOException, ParsingException {
        // Script with only Hide and no Show - should not inject
        String script = """
                when green flag clicked
                move (10) steps
                hide
                """;

        Program program = parseScratchBlocks(script);

        HideWithoutShowInjector injector = new HideWithoutShowInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertTrue(injected.isEmpty(), "Should not inject when there is no Show to remove");
    }

    @Test
    public void testNoHideNoInjection() throws IOException, ParsingException {
        // Script with Show but no Hide - precondition not met
        String script = """
                when green flag clicked
                show
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        HideWithoutShowInjector injector = new HideWithoutShowInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertTrue(injected.isEmpty(), "Should not inject without Hide (precondition not met)");
    }

    private boolean hasShow(Program program) {
        AtomicBoolean found = new AtomicBoolean(false);
        program.accept(new ScratchVisitor() {
            @Override
            public void visit(Show node) {
                found.set(true);
            }
        });
        return found.get();
    }
}
