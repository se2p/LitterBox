package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.SplitScriptAfterUntilFinder;
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

public class SplitScriptAfterUntilTest implements JsonTest {

    @Test
    public void testSplitScriptAfterUntilFinder() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/splitScriptAfterUntil.json");
        SplitScriptAfterUntilFinder finder = new SplitScriptAfterUntilFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).hasSize(1);
        assertThat(refactorings.get(0)).isInstanceOf(SplitScriptAfterUntil.class);
    }

    @Test
    public void testSplitScriptAfterUntilRefactoring() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/splitScriptAfterUntil.json");
        Script script = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        StmtList stmtList = script.getStmtList();
        UntilStmt untilStmt = (UntilStmt) stmtList.getStatement(0);

        SplitScriptAfterUntil refactoring = new SplitScriptAfterUntil(script, untilStmt);
        Program refactored = refactoring.apply(program);

        Script refactoredScript1 = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        Script refactoredScript2 = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(1);

        StmtList refactoredStmtList1 = refactoredScript1.getStmtList();
        StmtList refactoredStmtList2 = refactoredScript2.getStmtList();
        assertThat(stmtList.getNumberOfStatements()).isEqualTo(refactoredStmtList1.getNumberOfStatements() + refactoredStmtList2.getNumberOfStatements() - 1);

        WaitUntil waitUntil = (WaitUntil) refactoredStmtList2.getStatement(0);
        assertThat(waitUntil.getUntil()).isEqualTo(untilStmt.getBoolExpr());
    }
}
