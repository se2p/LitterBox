/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.SequenceToLoopFinder;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.TurnRight;
import de.uni_passau.fim.se2.litterbox.export.scratchblocks.ScratchBlocksVisitor;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SequenceToLoopTest implements JsonTest {

    @Test
    public void testSequenceToLoopFinder() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/unrolledLoop.json");
        SequenceToLoopFinder finder = new SequenceToLoopFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).hasSize(1);
        assertThat(refactorings.getFirst()).isInstanceOf(SequenceToLoop.class);
    }

    @Test
    public void testSequenceToLoopRefactoring() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/unrolledLoop.json");
        Script script = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().getFirst();
        StmtList stmtList = script.getStmtList();
        List<Stmt> subsequence = stmtList.getStmts().subList(1, 3);
        int times = 3;

        SequenceToLoop refactoring = new SequenceToLoop(stmtList, subsequence, times);
        Program refactored = refactoring.apply(program);

        Script refactoredScript = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().getFirst();
        StmtList refactoredStmtList = refactoredScript.getStmtList();
        assertThat(refactoredStmtList.getNumberOfStatements()).isEqualTo(stmtList.getNumberOfStatements() + 1 - times * subsequence.size());
    }

    @Test   
    void testBuggySequence() throws IOException, ParsingException {
        String code = """
        when green flag clicked
        turn cw (15) degrees
        repeat (2)
            move (10) steps
        end
        turn cw (15) degrees
        turn cw (15) degrees
        """;
        Program program = parseScratchBlocks(code);
        SequenceToLoopFinder sequenceToLoopFinder = new SequenceToLoopFinder();
        List<Refactoring> refactorings = sequenceToLoopFinder.check(program);
        assertEquals(1, refactorings.size());
        Program result = refactorings.getFirst().apply(program);
        String resultCode = ScratchBlocksVisitor.of(result);
        System.out.println(resultCode);
    }

    @Test
    void testBuggySequence2() throws IOException, ParsingException {
        String code = """
        when green flag clicked
        repeat (2)
            move (10) steps
        end
        turn right (15) degrees
        move (10) steps
        turn right (15) degrees
        turn right (15) degrees
        """;
        Program program = parseScratchBlocks(code);
        SequenceToLoopFinder sequenceToLoopFinder = new SequenceToLoopFinder();
        List<Refactoring> refactorings = sequenceToLoopFinder.check(program);
        assertEquals(1, refactorings.size());
        Program result = refactorings.getFirst().apply(program);
        String resultCode = ScratchBlocksVisitor.of(result);
        System.out.println(resultCode);
        List<Stmt> statements = result.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().getFirst().getStmtList().getStmts();
        assertTrue(statements.getFirst() instanceof RepeatTimesStmt);
        assertTrue(statements.get(1) instanceof TurnRight);
        assertTrue(statements.get(2) instanceof MoveSteps);
        assertTrue(statements.get(3) instanceof RepeatTimesStmt);
    }
}
