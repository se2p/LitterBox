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
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.SwapStatementsFinder;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class SwapStatementsTest implements JsonTest {

    @Test
    public void testSwapStatementsFinder() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/swappable.json");
        SwapStatementsFinder finder = new SwapStatementsFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).hasSize(1);
        assertThat(refactorings.get(0)).isInstanceOf(SwapStatements.class);
    }

    @Test
    public void testSwapStatementRefactoring() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/swappable.json");
        Script script = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);

        SwapStatementsFinder finder = new SwapStatementsFinder();
        List<Refactoring> refactorings = finder.check(program);

        SwapStatements refactoring = (SwapStatements) refactorings.get(0);
        Program refactored = refactoring.apply(program);

        Script refactoredScript = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        assertThat(refactoredScript.getEvent()).isEqualTo(script.getEvent());
        assertThat(refactoredScript.getStmtList().getNumberOfStatements()).isEqualTo(script.getStmtList().getNumberOfStatements());
        assertThat(refactoredScript.getStmtList().getStmts()).containsExactlyElementsIn(script.getStmtList().getStmts());

        Stmt move1 = script.getStmtList().getStmts().stream().filter(s -> s instanceof MoveSteps).findFirst().get();
        Stmt move2 = refactoredScript.getStmtList().getStmts().stream().filter(s -> s instanceof MoveSteps).findFirst().get();

        Stmt set1 = script.getStmtList().getStmts().stream().filter(s -> s instanceof SetVariableTo).findFirst().get();
        Stmt set2 = refactoredScript.getStmtList().getStmts().stream().filter(s -> s instanceof SetVariableTo).findFirst().get();

        assertThat(script.getStmtList().getStmts().indexOf(move1)).isEqualTo(refactoredScript.getStmtList().getStmts().indexOf(set2));
        assertThat(script.getStmtList().getStmts().indexOf(set1)).isEqualTo(refactoredScript.getStmtList().getStmts().indexOf(move2));
    }
}
