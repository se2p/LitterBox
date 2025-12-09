/*
 * Copyright (C) 2019-2025 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.ExtractLoopConditionFinder;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.SplitScriptFinder;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Not;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class ExtractLoopConditionTest implements JsonTest {

    @Test
    public void testExtractLoopConditionFinder() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/extractLoopCondition.json");
        ExtractLoopConditionFinder finder = new ExtractLoopConditionFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).hasSize(1);
        assertThat(refactorings.getFirst()).isInstanceOf(ExtractLoopCondition.class);
    }

    @Test
    public void testExtractLoopConditionRefactoring() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/extractLoopCondition.json");
        Script script = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().getFirst();
        StmtList stmtList = script.getStmtList();
        RepeatForeverStmt loopStmt = (RepeatForeverStmt) stmtList.getStatement(0);
        IfThenStmt ifStmt = (IfThenStmt) loopStmt.getStmtList().getStmts().stream().filter(s -> s instanceof IfThenStmt).findFirst().get();

        ExtractLoopCondition refactoring = new ExtractLoopCondition(loopStmt, ifStmt);
        Program refactored = refactoring.apply(program);

        Script refactoredScript = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().getFirst();
        StmtList refactoredStmtList = refactoredScript.getStmtList();
        assertThat(refactoredStmtList.getNumberOfStatements()).isEqualTo(2);

        UntilStmt untilStmt = (UntilStmt) refactoredStmtList.getStatement(0);
        assertThat(untilStmt.getBoolExpr()).isEqualTo(ifStmt.getBoolExpr());
        assertThat(untilStmt.getStmtList().getNumberOfStatements()).isEqualTo(1);
    }


    @Test
    public void testExtractLoopConditionRefactoringGeneralised() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/extractLoopConditionGeneralization.json");
        Script script = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().getFirst();
        StmtList stmtList = script.getStmtList();
        RepeatForeverStmt loopStmt = (RepeatForeverStmt) stmtList.getStatement(0);
        IfThenStmt ifStmt = (IfThenStmt) loopStmt.getStmtList().getStmts().stream().filter(s -> s instanceof IfThenStmt).findFirst().get();

        ExtractLoopCondition refactoring = new ExtractLoopCondition(loopStmt, ifStmt);
        Program refactored = refactoring.apply(program);

        Script refactoredScript = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().getFirst();
        StmtList refactoredStmtList = refactoredScript.getStmtList();
        
        assertThat(refactoredStmtList.getNumberOfStatements()).isEqualTo(3);

        UntilStmt untilStmt = (UntilStmt) refactoredStmtList.getStatement(0);
        assertThat(untilStmt.getBoolExpr()).isEqualTo(ifStmt.getBoolExpr());
        assertThat(untilStmt.getStmtList().getNumberOfStatements()).isEqualTo(1); // Only "say Hello!" inside loop        
        assertThat(refactoredStmtList.getStatement(1)).isInstanceOf(Say.class);
    }

    @Test
    public void testExtractLoopConditionRefactoringIfElse() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/extractLoopConditionIfElse.json");
        Script script = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().getFirst();
        StmtList stmtList = script.getStmtList();
        RepeatForeverStmt loopStmt = (RepeatForeverStmt) stmtList.getStatement(0);
        IfElseStmt ifStmt = (IfElseStmt) loopStmt.getStmtList().getStmts().stream().filter(s -> s instanceof IfElseStmt).findFirst().get();

        ExtractLoopCondition refactoring = new ExtractLoopCondition(loopStmt, ifStmt);
        Program refactored = refactoring.apply(program);

        Script refactoredScript = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().getFirst();
        StmtList refactoredStmtList = refactoredScript.getStmtList();

        // Original loop body:
        // Say Hello! (2s)
        // If <key space pressed> then
        //   Stop all
        // else
        //   Say Else (1s)
        
        // Refactored:
        // Repeat until <key space pressed>
        //   Say Hello! (2s)
        //   Say Else (1s)
        // Stop all
                
        assertThat(refactoredStmtList.getNumberOfStatements()).isEqualTo(2);

        UntilStmt untilStmt = (UntilStmt) refactoredStmtList.getStatement(0);
        assertThat(untilStmt.getBoolExpr()).isEqualTo(ifStmt.getBoolExpr());
        
        // Inside loop:
        // 1. Say Hello! (2s) - from original loop body before IfElse
        // 2. Say Else (1s) - from else block
        assertThat(untilStmt.getStmtList().getNumberOfStatements()).isEqualTo(2);
        assertThat(untilStmt.getStmtList().getStatement(1)).isInstanceOf(SayForSecs.class); // Say Else
    }

    @Test
    public void testExtractLoopConditionRefactoringIfElseNegated() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/extractLoopConditionIfElseNegated.json");
        Script script = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().getFirst();
        StmtList stmtList = script.getStmtList();
        RepeatForeverStmt loopStmt = (RepeatForeverStmt) stmtList.getStatement(0);
        IfElseStmt ifStmt = (IfElseStmt) loopStmt.getStmtList().getStmts().stream().filter(s -> s instanceof IfElseStmt).findFirst().get();

        ExtractLoopCondition refactoring = new ExtractLoopCondition(loopStmt, ifStmt);
        Program refactored = refactoring.apply(program);

        Script refactoredScript = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().getFirst();
        StmtList refactoredStmtList = refactoredScript.getStmtList();

        assertThat(refactoredStmtList.getNumberOfStatements()).isEqualTo(2);

        UntilStmt untilStmt = (UntilStmt) refactoredStmtList.getStatement(0);
        assertThat(untilStmt.getBoolExpr()).isInstanceOf(Not.class);
        Not notExpr = (Not) untilStmt.getBoolExpr();
        assertThat(notExpr.getOperand1()).isEqualTo(ifStmt.getBoolExpr());

        assertThat(untilStmt.getStmtList().getNumberOfStatements()).isEqualTo(2);
        assertThat(untilStmt.getStmtList().getStatement(1)).isInstanceOf(SayForSecs.class); // Say Then
    }


    @Test
    public void testExtractLoopConditionRefactoringGeneralization() throws ParsingException, IOException {
        String code = """
                when green flag clicked
                set y to (170)
                set x to (pick random (-220) to (220))
                forever
                if <touching (Bowl v) ?> then
                change [Punkte v] by (5)
                set y to (170)
                set x to (pick random (-220) to (220))
                end
                if <touching color [#be362e] ?> then
                say [Hello!]
                stop [all v]
                else
                change y by (-5)
                end
                end
                """;
        Program program = parseScratchBlocks(code);
        ExtractLoopConditionFinder finder = new ExtractLoopConditionFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).hasSize(1);
    }
}
