/*
 * Copyright (C) 2019-2021 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.MergeScriptsAfterUntilFinder;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class MergeScriptsAfterUntilTest implements JsonTest {

    @Test
    public void testSplitScriptAfterUntilFinder() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/mergeScriptsAfterUntil.json");
        MergeScriptsAfterUntilFinder finder = new MergeScriptsAfterUntilFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).hasSize(1);
        assertThat(refactorings.get(0)).isInstanceOf(MergeScriptsAfterUntil.class);
    }

    @Test
    public void testSplitScriptAfterUntilRefactoring() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/mergeScriptsAfterUntil.json");
        Script script1 = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        Script script2 = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(1);
        StmtList stmtList1 = script1.getStmtList();
        StmtList stmtList2 = script2.getStmtList();
        UntilStmt untilStmt = (UntilStmt) stmtList1.getStatement(0);

        MergeScriptsAfterUntil refactoring = new MergeScriptsAfterUntil(script1, script2, untilStmt);
        Program refactored = refactoring.apply(program);

        assertThat(refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().size()).isEqualTo(1);
        Script refactoredScript = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);

        StmtList refactoredStmtList = refactoredScript.getStmtList();
        assertThat(refactoredStmtList.getNumberOfStatements()).isEqualTo(script1.getStmtList().getNumberOfStatements() + script2.getStmtList().getNumberOfStatements() - 1);
    }
}
