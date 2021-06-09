package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.ExtractLoopConditionFinder;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.InlineLoopConditionFinder;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class InlineLoopConditionTest implements JsonTest {

    @Test
    public void testInlineLoopConditionFinder() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/inlineLoopCondition.json");
        InlineLoopConditionFinder finder = new InlineLoopConditionFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).hasSize(1);
        assertThat(refactorings.get(0)).isInstanceOf(InlineLoopCondition.class);
    }

    @Test
    public void testInlineLoopConditionRefactoring() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/inlineLoopCondition.json");
        Script script = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        StmtList stmtList = script.getStmtList();
        UntilStmt loopStmt = (UntilStmt) stmtList.getStmts().stream().filter(s -> s instanceof UntilStmt).findFirst().get();
        InlineLoopCondition refactoring = new InlineLoopCondition(loopStmt);
        Program refactored = refactoring.apply(program);

        Script refactoredScript = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        StmtList refactoredStmtList = refactoredScript.getStmtList();
        assertThat(refactoredStmtList.getNumberOfStatements()).isEqualTo(2);

        RepeatForeverStmt foreverStmt = (RepeatForeverStmt) refactoredScript.getStmtList().getStmts().stream().filter(s -> s instanceof RepeatForeverStmt).findFirst().get();
        assertThat(foreverStmt.getStmtList().getNumberOfStatements()).isEqualTo(2);
        IfThenStmt ifThenStmt = (IfThenStmt) foreverStmt.getStmtList().getStmts().stream().filter(s -> s instanceof IfThenStmt).findFirst().get();
        assertThat(ifThenStmt.getBoolExpr().equals(loopStmt.getBoolExpr()));
        assertThat(ifThenStmt.getThenStmts().getNumberOfStatements()).isEqualTo(1);
        assertThat(ifThenStmt.getThenStmts().getStatement(0)).isInstanceOf(StopThisScript.class);
    }
}
