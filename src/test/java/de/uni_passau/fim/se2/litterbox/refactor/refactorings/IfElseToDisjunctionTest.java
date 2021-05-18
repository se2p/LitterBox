package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.IfElseToDisjunctionFinder;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Or;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class IfElseToDisjunctionTest implements JsonTest {

    @Test
    public void testIfElseToDisjunctionFinder() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/ifElseToDisjunction.json");
        IfElseToDisjunctionFinder finder = new IfElseToDisjunctionFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).hasSize(1);
        assertThat(refactorings.get(0)).isInstanceOf(IfElseToDisjunction.class);
    }

    @Test
    public void testIfElseToDisjunctionRefactoring() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/ifElseToDisjunction.json");
        Script script = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        StmtList stmtList = script.getStmtList();
        IfElseStmt if1 = (IfElseStmt) stmtList.getStatement(0);
        IfThenStmt if2 = (IfThenStmt) if1.getElseStmts().getStatement(0);

        IfElseToDisjunction refactoring = new IfElseToDisjunction(if1, if2);
        Program refactored = refactoring.apply(program);

        Script refactoredScript = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        StmtList refactoredStmtList = refactoredScript.getStmtList();
        assertThat(refactoredStmtList.getNumberOfStatements()).isEqualTo(1);

        IfThenStmt if3 = (IfThenStmt) refactoredStmtList.getStatement(0);
        Or disjunction = (Or) if3.getBoolExpr();
        assertThat(disjunction.getOperand1()).isEqualTo(if1.getBoolExpr());
        assertThat(disjunction.getOperand2()).isEqualTo(if2.getBoolExpr());
        assertThat(if3.getThenStmts()).isEqualTo(if1.getThenStmts());
        assertThat(if3.getThenStmts()).isEqualTo(if2.getThenStmts());
    }
}
