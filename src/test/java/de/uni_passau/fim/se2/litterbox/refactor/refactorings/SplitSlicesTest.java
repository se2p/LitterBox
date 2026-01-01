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
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.SplitSliceFinder;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.export.scratchblocks.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.scratchblocks.ScratchBlocksParser;

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
        assertThat(refactorings.getFirst()).isInstanceOf(SplitSlice.class);
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
        assertThat(refactorings).hasSize(1);
    }

    @Test
    public void testSplitSliceRefactoring() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/sliceable.json");
        Script script = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().getFirst();

        SplitSliceFinder finder = new SplitSliceFinder();
        List<Refactoring> refactorings = finder.check(program);

        SplitSlice refactoring = (SplitSlice) refactorings.getFirst();
        Program refactored = refactoring.apply(program);

        Script refactoredScript1 = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().getFirst();
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
        Script script = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().getFirst();

        SplitSliceFinder finder = new SplitSliceFinder();
        List<Refactoring> refactorings = finder.check(program);

        SplitSlice refactoring = (SplitSlice) refactorings.getFirst();
        Program refactored = refactoring.apply(program);

        Script refactoredScript1 = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().getFirst();
        Script refactoredScript2 = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(1);
        Script refactoredScript3 = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(2);

        assertThat(refactoredScript1.getEvent()).isEqualTo(script.getEvent());
        assertThat(refactoredScript2.getEvent()).isEqualTo(script.getEvent());
        assertThat(refactoredScript3.getEvent()).isEqualTo(script.getEvent());

        StmtList refactoredStmtList1 = refactoredScript1.getStmtList();
        StmtList refactoredStmtList2 = refactoredScript2.getStmtList();
        StmtList refactoredStmtList3 = refactoredScript3.getStmtList();
        List<Integer> sizes = new java.util.ArrayList<>();
        sizes.add(refactoredStmtList1.getStmts().size());
        sizes.add(refactoredStmtList2.getStmts().size());
        sizes.add(refactoredStmtList3.getStmts().size());
        sizes.sort(Integer::compareTo);
        
        assertThat(sizes).containsExactly(1, 1, 5);
    }

    @Test
    public void testSplitSliceFinder_SplitsForeverLoop() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        ScratchBlocksParser parser = new ScratchBlocksParser();
        String code = """
            when green flag clicked
            forever 
                move (10) steps
                set [my variable v] to (0)
                change [my variable v] by (1)
                say (my variable)
            end
            """;

        Program newProgram = parser.extendProject(program, "Sprite1", code);
        SplitSliceFinder finder = new SplitSliceFinder();
        List<Refactoring> refactorings = finder.check(newProgram);

        String expected1 = """
            when green flag clicked
            forever 
                move (10) steps
            end
            """;

        String expected2 = """
            when green flag clicked
            forever 
                set [my variable v] to (0)
                change [my variable v] by (1)
                say (my variable)
            end
            """;

        assertThat(refactorings).hasSize(1);

        Program refactored = refactorings.getFirst().apply(newProgram);
        String refactoredCode = ScratchBlocksVisitor.of(refactored);
        assertThat(refactoredCode.replaceAll("\\s+", "")).contains(expected1.replaceAll("\\s+", ""));
        assertThat(refactoredCode.replaceAll("\\s+", "")).contains(expected2.replaceAll("\\s+", ""));
    }

    @Test
    public void testSplitSliceFinder_SplitsNestedForeverLoops() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        ScratchBlocksParser parser = new ScratchBlocksParser();
        String code = """
            when green flag clicked
            forever
                forever 
                    move (10) steps
                    set [my variable v] to (0)
                    change [my variable v] by (1)
                    say (my variable)
                end
            end
            """;
        Program newProgram = parser.extendProject(program, "Sprite1", code);
        SplitSliceFinder finder = new SplitSliceFinder();
        List<Refactoring> refactorings = finder.check(newProgram);
        assertThat(refactorings).hasSize(1);

        String expected1 = """
            when green flag clicked
            forever 
                forever 
                    move (10) steps
                end
            end
            """;

        String expected2 = """
            when green flag clicked
            forever 
                forever 
                    set [my variable v] to (0)
                    change [my variable v] by (1)
                    say (my variable)
                end
            end
            """;

        Program refactored = refactorings.getFirst().apply(newProgram);
        String refactoredCode = ScratchBlocksVisitor.of(refactored);
        assertThat(refactoredCode.replaceAll("\\s+", "")).contains(expected1.replaceAll("\\s+", ""));
        assertThat(refactoredCode.replaceAll("\\s+", "")).contains(expected2.replaceAll("\\s+", ""));
    }

    @Test
    public void testSplitSliceFinder_DoesNotSplitLoopWithConditionDependency() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        ScratchBlocksParser parser = new ScratchBlocksParser();
        String code = """
            when green flag clicked
            repeat until <(my variable) = (10)> 
                move (10) steps
                set [my variable v] to (0)
                change [my variable v] by (1)
                say (my variable)
            end
            """;
        Program newProgram = parser.extendProject(program, "Sprite1", code);
        SplitSliceFinder finder = new SplitSliceFinder();
        List<Refactoring> refactorings = finder.check(newProgram);
        assertThat(refactorings).isEmpty();
    }


    @Test
    public void testSplitSliceFinder_SplitsIfStatementAndSubsequentStatement() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        ScratchBlocksParser parser = new ScratchBlocksParser();
        String code = """
            when green flag clicked
            if <(my variable v) = (10)> then
                set [my variable v] to (0)
                change [my variable v] by (1)
                say (my variable)
            end
            move (10) steps
            """;
        Program newProgram = parser.extendProject(program, "Sprite1", code);
        SplitSliceFinder finder = new SplitSliceFinder();
        List<Refactoring> refactorings = finder.check(newProgram);
        assertThat(refactorings).hasSize(1);

        String expected1 = """
            when green flag clicked
            move (10) steps
            """;

        String expected2 = """
            when green flag clicked
            if <\\(my variable v\\) = \\(10\\)> then
                set [my variable v] to (0)
                change [my variable v] by (1)
                say (my variable)
            end
            """;

        Program refactored = refactorings.getFirst().apply(newProgram);
        String refactoredCode = ScratchBlocksVisitor.of(refactored);
        assertThat(refactoredCode.replaceAll("\\s+", "")).contains(expected1.replaceAll("\\s+", ""));
        assertThat(refactoredCode.replaceAll("\\s+", "")).contains(expected2.replaceAll("\\s+", ""));
      }

    @Test
    public void testSplitSliceFinder_SplitsIfElseIntoIndependentIfs() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        ScratchBlocksParser parser = new ScratchBlocksParser();
        String code = """
            when green flag clicked
            if <(my variable v) = (10)> then
                set [my variable v] to (0)
                change [my variable v] by (1)
                say (my variable)
            else
                move (10) steps            
            end
            """;
        Program newProgram = parser.extendProject(program, "Sprite1", code);
        SplitSliceFinder finder = new SplitSliceFinder();
        List<Refactoring> refactorings = finder.check(newProgram);
        assertThat(refactorings).hasSize(1);

        String expected1 = """
            when green flag clicked
            if <not <\\(my variable v\\) = \\(10\\)>> then
                move (10) steps
            end
            """;

        String expected2 = """
            when green flag clicked
            if <\\(my variable v\\) = \\(10\\)> then
                set [my variable v] to (0)
                change [my variable v] by (1)
                say (my variable)
            end
            """;

        Program refactored = refactorings.getFirst().apply(newProgram);
        String refactoredCode = ScratchBlocksVisitor.of(refactored);
        assertThat(refactoredCode.replaceAll("\\s+", "")).contains(expected1.replaceAll("\\s+", ""));
        assertThat(refactoredCode.replaceAll("\\s+", "")).contains(expected2.replaceAll("\\s+", ""));
      }

    @Test
    public void testSplitSliceFinder_SplitsLoopWithIfElse() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        ScratchBlocksParser parser = new ScratchBlocksParser();
        String code = """
            when green flag clicked
            wait (10) seconds
            repeat (10)
                set [my variable v] to (0)
                change [my variable v] by (1)
                if <(my other variable v) = (10)> then
                    say (my variable)
                else
                    move (10) steps
                end
            end
            """;
        Program newProgram = parser.extendProject(program, "Sprite1", code);
        SplitSliceFinder finder = new SplitSliceFinder();
        List<Refactoring> refactorings = finder.check(newProgram);
        assertThat(refactorings).hasSize(1);

        String expected1 = """
            when green flag clicked
            wait (10) seconds
            repeat (10)
                if <not <\\(my other variable v\\) = \\(10\\)>> then
                    move (10) steps
                end
            end
            """;

        String expected2 = """
            when green flag clicked
            wait (10) seconds
            repeat (10)
                set [my variable v] to (0)
                change [my variable v] by (1)
                if <\\(my other variable v\\) = \\(10\\)> then
                    say (my variable)
                end
            end
            """;

        Program refactored = refactorings.getFirst().apply(newProgram);
        String refactoredCode = ScratchBlocksVisitor.of(refactored);
        assertThat(refactoredCode.replaceAll("\\s+", "")).contains(expected1.replaceAll("\\s+", ""));
        assertThat(refactoredCode.replaceAll("\\s+", "")).contains(expected2.replaceAll("\\s+", ""));
    }

    @Test
    public void testSplitSliceFinder_SplitsIfElseDependentOnAnswer() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        ScratchBlocksParser parser = new ScratchBlocksParser();
        String code = """
            when green flag clicked
            ask [Question] and wait
            if <(answer) = [yes]> then
                say [Hello]
            else
                move (10) steps
            end
            """;
        Program newProgram = parser.extendProject(program, "Sprite1", code);
        SplitSliceFinder finder = new SplitSliceFinder();
        List<Refactoring> refactorings = finder.check(newProgram);
        assertThat(refactorings).hasSize(1);

        String expected1 = """
            when green flag clicked
            ask [Question] and wait
            if <(answer) = [yes]> then
                say [Hello]
            end
            """;

        String expected2 = """
            when green flag clicked
            ask [Question] and wait
            if <not <(answer) = [yes]>> then
                move (10) steps
            end
            """;

        Program refactored = refactorings.getFirst().apply(newProgram);
        String refactoredCode = ScratchBlocksVisitor.of(refactored);
        assertThat(refactoredCode.replaceAll("\\s+", "")).contains(expected1.replaceAll("\\s+", ""));
        assertThat(refactoredCode.replaceAll("\\s+", "")).contains(expected2.replaceAll("\\s+", ""));
    }

    @Test
    public void testSplitSliceFinder_DoesNotSplitLoopWithXPositionDependency() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        ScratchBlocksParser parser = new ScratchBlocksParser();
        String code = """
            when green flag clicked
            repeat until <(x position) > (100)>
                move (10) steps
                say [Hello]
            end
            """;
        Program newProgram = parser.extendProject(program, "Sprite1", code);
        SplitSliceFinder finder = new SplitSliceFinder();
        List<Refactoring> refactorings = finder.check(newProgram);
        assertThat(refactorings).isEmpty();
    }

}
