package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.ExtractEventsFromForeverFinder;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExtractEventsFromForeverTest implements JsonTest {

    @Test
    public void testExtractEventHandlerFinder() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/extractEventHandler.json");
        ExtractEventsFromForeverFinder finder = new ExtractEventsFromForeverFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).hasSize(1);
    }

    @Test
    public void testExtractEventHandler() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/extractEventHandler.json");
        ExtractEventsFromForeverFinder finder = new ExtractEventsFromForeverFinder();
        List<Refactoring> refactorings = finder.check(program);
        Refactoring r = refactorings.get(0);
        Program refactored = r.apply(program);
        assertThat(program).isNotEqualTo(refactored);
    }

    @Test
    public void testExtractEventHandlerCheckProgram() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/extractEventHandler.json");
        ExtractEventsFromForeverFinder finder = new ExtractEventsFromForeverFinder();
        List<Refactoring> refactorings = finder.check(program);
        Refactoring r = refactorings.get(0);
        Program refactored = r.apply(program);
        Script refactoredScriptEvent1 = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        Script refactoredScriptEvent2 = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(1);
        assertTrue(refactoredScriptEvent1.getEvent() instanceof KeyPressed);
        assertTrue(refactoredScriptEvent2.getEvent() instanceof KeyPressed);
    }

    @Test
    public void testExtractEventHandlerFinderWithNotIfInLoop() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/extractEventFromForeverWithNotIf.json");
        ExtractEventsFromForeverFinder finder = new ExtractEventsFromForeverFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).isEmpty();
    }
}
