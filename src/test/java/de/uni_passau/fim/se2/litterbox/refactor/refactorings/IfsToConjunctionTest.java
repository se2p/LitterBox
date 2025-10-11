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
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.IfsToConjunctionFinder;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.And;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class IfsToConjunctionTest implements JsonTest {

    @Test
    public void testIfsToConjunctionFinder() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/ifsToConjunction.json");
        IfsToConjunctionFinder finder = new IfsToConjunctionFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).hasSize(1);
        assertThat(refactorings.getFirst()).isInstanceOf(IfsToConjunction.class);
    }

    @Test
    public void testIfsToConjunctionRefactoring() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/ifsToConjunction.json");
        Script script = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().getFirst();
        StmtList stmtList = script.getStmtList();
        IfThenStmt if1 = (IfThenStmt) stmtList.getStatement(0);
        IfThenStmt if2 = (IfThenStmt) if1.getThenStmts().getStatement(0);

        IfsToConjunction refactoring = new IfsToConjunction(if1, if2);
        Program refactored = refactoring.apply(program);

        Script refactoredScript = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().getFirst();
        StmtList refactoredStmtList = refactoredScript.getStmtList();
        assertThat(refactoredStmtList.getNumberOfStatements()).isEqualTo(1);

        IfThenStmt if3 = (IfThenStmt) refactoredStmtList.getStatement(0);
        And conjunction = (And) if3.getBoolExpr();
        assertThat(conjunction.getOperand1()).isEqualTo(if1.getBoolExpr());
        assertThat(conjunction.getOperand2()).isEqualTo(if2.getBoolExpr());
        assertThat(if3.getThenStmts()).isEqualTo(if2.getThenStmts());
    }

}
