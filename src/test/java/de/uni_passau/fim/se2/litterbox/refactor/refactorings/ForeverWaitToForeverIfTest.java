package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.ForeverWaitToForeverIfFinder;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class ForeverWaitToForeverIfTest implements JsonTest {

        @Test
        public void testForeverWaitToForeverIfFinder() throws ParsingException, IOException {
            Program program = getAST("src/test/fixtures/refactoring/foreverWaitToForeverIf.json");
            ForeverWaitToForeverIfFinder finder = new ForeverWaitToForeverIfFinder();
            List<Refactoring> refactorings = finder.check(program);
            assertThat(refactorings).hasSize(1);
            assertThat(refactorings.get(0)).isInstanceOf(ForeverWaitToForeverIf.class);
        }

        @Test
        public void testForeverWaitToForeverIfRefactoring() throws ParsingException, IOException {
            Program program = getAST("src/test/fixtures/refactoring/foreverWaitToForeverIf.json");
            Script script = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
            StmtList stmtList = script.getStmtList();
            RepeatForeverStmt loopStmt = (RepeatForeverStmt) stmtList.getStmts().stream().filter(s -> s instanceof RepeatForeverStmt).findFirst().get();
            WaitUntil waitUntil = (WaitUntil) loopStmt.getStmtList().getStatement(0);

            ForeverWaitToForeverIf refactoring = new ForeverWaitToForeverIf(loopStmt);
            Program refactored = refactoring.apply(program);

            Script refactoredScript = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
            StmtList refactoredStmtList = refactoredScript.getStmtList();
            assertThat(refactoredStmtList.getNumberOfStatements()).isEqualTo(1);

            RepeatForeverStmt foreverStmt = (RepeatForeverStmt) refactoredScript.getStmtList().getStmts().stream().filter(s -> s instanceof RepeatForeverStmt).findFirst().get();
            assertThat(foreverStmt.getStmtList().getNumberOfStatements()).isEqualTo(1);

            IfThenStmt ifThenStmt = (IfThenStmt) foreverStmt.getStmtList().getStatement(0);
            assertThat(ifThenStmt.getBoolExpr().equals(waitUntil.getUntil()));

            assertThat(ifThenStmt.getThenStmts().getNumberOfStatements()).isEqualTo(loopStmt.getStmtList().getNumberOfStatements() - 1);
        }
}
