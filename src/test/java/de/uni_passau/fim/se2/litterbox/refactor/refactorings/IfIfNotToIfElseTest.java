package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.IfIfNotToIfElseFinder;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Not;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class IfIfNotToIfElseTest implements JsonTest {

    @Test
    public void testIfIfNotToIfElseFinder() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/ififnot.json");
        IfIfNotToIfElseFinder finder = new IfIfNotToIfElseFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).hasSize(1);
        assertThat(refactorings.get(0)).isInstanceOf(IfIfNotToIfElse.class);
    }

    @Test
    public void testIfIfNotToIfElseRefactoring() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/ififnot.json");
        Script script = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        StmtList stmtList = script.getStmtList();
        IfThenStmt if1 = (IfThenStmt) stmtList.getStatement(0);
        IfThenStmt if2 = (IfThenStmt) stmtList.getStatement(1);
        Not negation = (Not) if2.getBoolExpr();

        IfIfNotToIfElse refactoring = new IfIfNotToIfElse(if1, if2);
        Program refactored = refactoring.apply(program);

        Script refactoredScript = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        StmtList refactoredStmtList = refactoredScript.getStmtList();
        assertThat(refactoredStmtList.getNumberOfStatements()).isEqualTo(1);

        IfElseStmt ifElse = (IfElseStmt) refactoredStmtList.getStatement(0);

        assertThat(if1.getBoolExpr()).isEqualTo(ifElse.getBoolExpr());
        assertThat(negation.getOperand1()).isEqualTo(ifElse.getBoolExpr());

        assertThat(if1.getThenStmts()).isEqualTo(ifElse.getThenStmts());
        assertThat(if2.getThenStmts()).isEqualTo(ifElse.getElseStmts());
    }
}
