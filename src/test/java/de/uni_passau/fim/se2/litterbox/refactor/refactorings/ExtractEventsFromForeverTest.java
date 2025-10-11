/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

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
        Refactoring r = refactorings.getFirst();
        Program refactored = r.apply(program);
        assertThat(program).isNotEqualTo(refactored);
    }

    @Test
    public void testExtractEventHandlerCheckProgram() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/extractEventHandler.json");
        ExtractEventsFromForeverFinder finder = new ExtractEventsFromForeverFinder();
        List<Refactoring> refactorings = finder.check(program);
        Refactoring r = refactorings.getFirst();
        Program refactored = r.apply(program);
        Script refactoredScriptEvent1 = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        Script refactoredScriptEvent2 = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(1);
        assertInstanceOf(KeyPressed.class, refactoredScriptEvent1.getEvent());
        assertInstanceOf(KeyPressed.class, refactoredScriptEvent2.getEvent());
    }

    @Test
    public void testExtractEventHandlerFinderWithNotIfInLoop() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/extractEventFromForeverWithNotIf.json");
        ExtractEventsFromForeverFinder finder = new ExtractEventsFromForeverFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).isEmpty();
    }
}
