package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.IfElseToConjunctionFinder;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.And;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class IfElseToConjunctionTest implements JsonTest {

    @Test
    public void testIfElseToConjunctionFinder() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/ifElseToConjunction.json");
        IfElseToConjunctionFinder finder = new IfElseToConjunctionFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).hasSize(1);
        assertThat(refactorings.get(0)).isInstanceOf(IfElseToConjunction.class);
    }

    @Test
    public void testIfElseToConjunctionRefactoring() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/ifElseToConjunction.json");
        Script script = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        StmtList stmtList = script.getStmtList();
        IfThenStmt if1 = (IfThenStmt) stmtList.getStatement(0);
        IfElseStmt if2 = (IfElseStmt) if1.getThenStmts().getStatement(0);

        IfElseToConjunction refactoring = new IfElseToConjunction(if1);
        Program refactored = refactoring.apply(program);

        Script refactoredScript = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        StmtList refactoredStmtList = refactoredScript.getStmtList();
        assertThat(refactoredStmtList.getNumberOfStatements()).isEqualTo(2);

        IfThenStmt if3 = (IfThenStmt) refactoredStmtList.getStatement(0);
        IfThenStmt if4 = (IfThenStmt) refactoredStmtList.getStatement(1);
        And conjunction = (And) if3.getBoolExpr();
        assertThat(conjunction.getOperand1()).isEqualTo(if1.getBoolExpr());
        assertThat(conjunction.getOperand2()).isEqualTo(if2.getBoolExpr());
        assertThat(if4.getBoolExpr()).isEqualTo(if1.getBoolExpr());
        assertThat(if3.getThenStmts()).isEqualTo(if2.getThenStmts());
        assertThat(if4.getThenStmts()).isEqualTo(if2.getElseStmts());
    }

}
