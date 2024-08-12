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
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.SplitLoopFinder;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.NextCostume;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class SplitLoopTest implements JsonTest {

    @Test
    public void testSplitLoopFinder() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/splittableLoop.json");
        SplitLoopFinder finder = new SplitLoopFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).hasSize(1);
        assertThat(refactorings.get(0)).isInstanceOf(SplitLoop.class);
    }

    @Test
    public void testSplitLoopFinder_ControlDependency() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/dependencyInLoop.json");
        SplitLoopFinder finder = new SplitLoopFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).isEmpty();
    }

    @Test
    public void testSplitLoopRefactoring() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/splittableLoop.json");
        Script script = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        StmtList stmtList = script.getStmtList();
        LoopStmt loop = (LoopStmt) stmtList.getStatement(0);

        SplitLoop refactoring = new SplitLoop(script, loop, loop.getStmtList().getStatement(1));
        Program refactored = refactoring.apply(program);

        Script refactoredScript1 = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        Script refactoredScript2 = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(1);
        assertThat(refactoredScript1.getEvent()).isEqualTo(script.getEvent());
        assertThat(refactoredScript2.getEvent()).isEqualTo(script.getEvent());

        StmtList refactoredStmtList1 = refactoredScript1.getStmtList();
        StmtList refactoredStmtList2 = refactoredScript2.getStmtList();
        assertThat(refactoredStmtList1.getStmts().size()).isEqualTo(1);
        assertThat(refactoredStmtList2.getStmts().size()).isEqualTo(1);

        LoopStmt refactoredLoop1 = (LoopStmt) refactoredStmtList1.getStatement(0);
        LoopStmt refactoredLoop2 = (LoopStmt) refactoredStmtList2.getStatement(0);
        assertThat(refactoredLoop1.getStmtList().getNumberOfStatements()).isEqualTo(1);
        assertThat(refactoredLoop2.getStmtList().getNumberOfStatements()).isEqualTo(1);

        assertThat(refactoredLoop1.getStmtList().getStatement(0)).isInstanceOf(ChangeVariableBy.class);
        assertThat(refactoredLoop2.getStmtList().getStatement(0)).isInstanceOf(NextCostume.class);
    }
}
