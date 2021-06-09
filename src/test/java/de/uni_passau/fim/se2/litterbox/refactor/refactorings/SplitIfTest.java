package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.SplitIfFinder;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class SplitIfTest implements JsonTest {

    @Test
    public void testSplitIfFinder() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/splittableIf.json");
        SplitIfFinder finder = new SplitIfFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).hasSize(1);
        assertThat(refactorings.get(0)).isInstanceOf(SplitIf.class);
    }


    @Test
    public void testSplitIfFinderMultipleBlocks() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/splittableIfMultipleBlocks.json");
        SplitIfFinder finder = new SplitIfFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).hasSize(3);
        assertThat(refactorings.get(0)).isInstanceOf(SplitIf.class);
        assertThat(refactorings.get(1)).isInstanceOf(SplitIf.class);
        assertThat(refactorings.get(2)).isInstanceOf(SplitIf.class);
    }


    @Test
    public void testSplitIfRefactoring() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/splittableIf.json");
        Script script = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        StmtList stmtList = script.getStmtList();

        IfThenStmt ifStatement1 = (IfThenStmt) stmtList.getStatement(0);

        SplitIf refactoring = new SplitIf(ifStatement1, ifStatement1.getThenStmts().getStatement(1));
        Program refactored = refactoring.apply(program);

        Script refactoredScript = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        StmtList refactoredStmtList = refactoredScript.getStmtList();
        assertThat(refactoredStmtList.getNumberOfStatements()).isEqualTo(2);

        IfThenStmt ifStatement2 = (IfThenStmt) refactoredStmtList.getStatement(0);
        IfThenStmt ifStatement3 = (IfThenStmt) refactoredStmtList.getStatement(1);

        assertThat(ifStatement2.getBoolExpr().equals(ifStatement1.getBoolExpr()));
        assertThat(ifStatement3.getBoolExpr().equals(ifStatement1.getBoolExpr()));

        assertThat(ifStatement2.getThenStmts().getStatement(0)).isEqualTo(ifStatement1.getThenStmts().getStatement(0));
        assertThat(ifStatement3.getThenStmts().getStatement(0)).isEqualTo(ifStatement1.getThenStmts().getStatement(1));
    }


    @Test
    public void testSplitIfRefactoringMultipleBlocks() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/splittableIfMultipleBlocks.json");
        Script script = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        StmtList stmtList = script.getStmtList();

        IfThenStmt ifStatement1 = (IfThenStmt) stmtList.getStatement(0);

        SplitIf refactoring = new SplitIf(ifStatement1, ifStatement1.getThenStmts().getStatement(2));
        Program refactored = refactoring.apply(program);

        Script refactoredScript = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        StmtList refactoredStmtList = refactoredScript.getStmtList();
        assertThat(refactoredStmtList.getNumberOfStatements()).isEqualTo(2);

        IfThenStmt ifStatement2 = (IfThenStmt) refactoredStmtList.getStatement(0);
        IfThenStmt ifStatement3 = (IfThenStmt) refactoredStmtList.getStatement(1);

        assertThat(ifStatement2.getBoolExpr().equals(ifStatement1.getBoolExpr()));
        assertThat(ifStatement3.getBoolExpr().equals(ifStatement1.getBoolExpr()));

        assertThat(ifStatement2.getThenStmts().getNumberOfStatements()).isEqualTo(2);
        assertThat(ifStatement3.getThenStmts().getNumberOfStatements()).isEqualTo(2);
    }
}
