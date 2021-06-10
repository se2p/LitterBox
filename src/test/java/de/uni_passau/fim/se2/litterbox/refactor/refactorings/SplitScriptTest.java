package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.SplitScriptFinder;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class SplitScriptTest implements JsonTest {

    @Test
    public void testSplitScriptFinder_DoNotSplitControlDependency() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/no_split_refactoring_control_dependency.json");
        SplitScriptFinder finder = new SplitScriptFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).isEmpty();
    }

    @Test
    public void testSplitScriptFinder_DoNotSplitDataDependency() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/no_split_refactoring_data_dependency.json");
        SplitScriptFinder finder = new SplitScriptFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).isEmpty();
    }

    @Test
    public void testSplitScriptFinder_DoNotSplitDataDependency_WhenMerged() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/no_split_refactoring_data_dependency_merged.json");
        SplitScriptFinder finder = new SplitScriptFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).isEmpty();
    }

    @Test
    public void testSplitScriptFinder_DoNotSplitTimeDependency() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/no_split_refactoring_time_dependency.json");
        SplitScriptFinder finder = new SplitScriptFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).isEmpty();
    }


    @Test
    public void testSplitScriptFinder_SplitMergedControlDependency() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/split_refactoring_control_dependency.json");
        SplitScriptFinder finder = new SplitScriptFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).hasSize(1);
    }

    @Test
    public void testSplitScriptFinder_SplitMergedTimeDependency() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/split_refactoring_time_dependency.json");
        SplitScriptFinder finder = new SplitScriptFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).hasSize(1);
    }


    @Test
    public void testSplitScriptFinder() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/splitScript.json");
        ControlFlowGraph cfg = getCFG("src/test/fixtures/refactoring/splitScript.json");
        System.out.println(cfg.toDotString());
        SplitScriptFinder finder = new SplitScriptFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).hasSize(1);
        assertThat(refactorings.get(0)).isInstanceOf(SplitScript.class);
    }

    @Test
    public void testSplitScriptFinder2() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/splitScript2.json");
        SplitScriptFinder finder = new SplitScriptFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).hasSize(1);
        assertThat(refactorings.get(0)).isInstanceOf(SplitScript.class);
    }

    @Test
    public void testSplitScriptFinder3() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/splitScript3.json");
        SplitScriptFinder finder = new SplitScriptFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).hasSize(1);
        assertThat(refactorings.get(0)).isInstanceOf(SplitScript.class);
    }

    @Test
    public void testSplitScriptRefactoring() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/splitScript.json");
        Script script = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        StmtList stmtList = script.getStmtList();

        WaitSeconds wait = (WaitSeconds)stmtList.getStmts().get(2);

        SplitScript refactoring = new SplitScript(script, wait);
        Program refactored = refactoring.apply(program);

        Script refactoredScript1 = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        Script refactoredScript2 = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(1);
        assertThat(refactoredScript1.getEvent()).isEqualTo(script.getEvent());
        assertThat(refactoredScript2.getEvent()).isEqualTo(script.getEvent());

        StmtList refactoredStmtList1 = refactoredScript1.getStmtList();
        StmtList refactoredStmtList2 = refactoredScript2.getStmtList();
        assertThat(refactoredStmtList1.getStmts().size()).isEqualTo(2);
        assertThat(refactoredStmtList2.getStmts().size()).isEqualTo(3);

        assertThat(refactoredStmtList2.getStmts().get(0)).isEqualTo(wait);
    }

}
