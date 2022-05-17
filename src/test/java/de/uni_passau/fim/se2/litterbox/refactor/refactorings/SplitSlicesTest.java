/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.SplitScriptFinder;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.SplitSliceFinder;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class SplitSlicesTest implements JsonTest {

    @Test
    public void testSplitSliceFinder() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/sliceable.json");
        SplitSliceFinder finder = new SplitSliceFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).hasSize(1);
        assertThat(refactorings.get(0)).isInstanceOf(SplitSlice.class);
    }

    @Test
    public void testSplitSliceFinder_Hide() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/splitSliceHide.json");
        SplitSliceFinder finder = new SplitSliceFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).isEmpty();
    }

    @Test
    public void testSplitSliceFinder_DoNotSplitEffectDependency() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/splitGraphicDependency.json");
        SplitSliceFinder finder = new SplitSliceFinder();
        List<Refactoring> refactorings = finder.check(program);
        // Splitting graphics effect and next costume is fine
        assertThat(refactorings).hasSize(1);
    }

    @Test
    public void testSplitSliceFinder_Wait() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/splitSliceWait.json");
        SplitSliceFinder finder = new SplitSliceFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).hasSize(1);
    }

    @Test
    public void testSplitSliceFinder_DependencyEdges() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/splitSlice2.json");
        SplitSliceFinder finder = new SplitSliceFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).isEmpty();
    }

    @Test
    public void testSplitSliceRefactoring() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/sliceable.json");
        Script script = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);

        SplitSliceFinder finder = new SplitSliceFinder();
        List<Refactoring> refactorings = finder.check(program);

        SplitSlice refactoring = (SplitSlice) refactorings.get(0);
        Program refactored = refactoring.apply(program);

        Script refactoredScript1 = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        Script refactoredScript2 = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(1);
        assertThat(refactoredScript1.getEvent()).isEqualTo(script.getEvent());
        assertThat(refactoredScript2.getEvent()).isEqualTo(script.getEvent());

        StmtList refactoredStmtList1 = refactoredScript1.getStmtList();
        StmtList refactoredStmtList2 = refactoredScript2.getStmtList();
        assertThat(refactoredStmtList1.getStmts().size()).isEqualTo(2);
        assertThat(refactoredStmtList2.getStmts().size()).isEqualTo(1);
    }

    @Test
    public void testSplitSliceRefactoring_Wait() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/splitSliceWait.json");
        Script script = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);

        SplitSliceFinder finder = new SplitSliceFinder();
        List<Refactoring> refactorings = finder.check(program);

        SplitSlice refactoring = (SplitSlice) refactorings.get(0);
        Program refactored = refactoring.apply(program);

        Script refactoredScript1 = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        Script refactoredScript2 = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(1);
        Script refactoredScript3 = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(2);

        assertThat(refactoredScript1.getEvent()).isEqualTo(script.getEvent());
        assertThat(refactoredScript2.getEvent()).isEqualTo(script.getEvent());
        assertThat(refactoredScript3.getEvent()).isEqualTo(script.getEvent());

        StmtList refactoredStmtList1 = refactoredScript1.getStmtList();
        StmtList refactoredStmtList2 = refactoredScript2.getStmtList();
        StmtList refactoredStmtList3 = refactoredScript3.getStmtList();
        assertThat(refactoredStmtList1.getStmts().size()).isEqualTo(1);
        assertThat(refactoredStmtList2.getStmts().size()).isEqualTo(1);
        assertThat(refactoredStmtList3.getStmts().size()).isEqualTo(5);
    }
}
