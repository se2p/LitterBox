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
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.MergeEventsIntoForeverFinder;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MergeEventsIntoForeverTest implements JsonTest {

    @Test
    public void testMergeEventHandlerFinder() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/mergeEventHandler.json");
        MergeEventsIntoForeverFinder finder = new MergeEventsIntoForeverFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).hasSize(1);
    }

    @Test
    public void testMergeEventHandler() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/mergeEventHandler.json");
        MergeEventsIntoForeverFinder finder = new MergeEventsIntoForeverFinder();
        List<Refactoring> refactorings = finder.check(program);
        Refactoring r = refactorings.get(0);
        Program refactored = r.apply(program);
        assertThat(program).isNotEqualTo(refactored);
    }

    @Test
    public void testMergeEventHandlerCheckProgram() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/mergeEventHandler.json");
        MergeEventsIntoForeverFinder finder = new MergeEventsIntoForeverFinder();
        List<Refactoring> refactorings = finder.check(program);
        Refactoring r = refactorings.get(0);
        Program refactored = r.apply(program);
        Script refactoredScript = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        List<Stmt> refactoredStmtList = refactoredScript.getStmtList().getStmts();
        assertEquals(1, refactoredStmtList.size());
        Stmt stmt = refactoredStmtList.get(0);
        assertTrue(stmt instanceof RepeatForeverStmt);
    }

}
