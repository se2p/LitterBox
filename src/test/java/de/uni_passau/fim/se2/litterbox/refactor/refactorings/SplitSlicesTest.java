package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.JsonTest;
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
    public void testSplitSliceRefactoring() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/sliceable.json");
        Script script = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        StmtList stmtList = script.getStmtList();

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
}
