package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.LoopUnrollingFinder;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class LoopUnrollingTest implements JsonTest {

    @Test
    public void testLoopUnrollingFinder() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/unrollableLoop.json");
        LoopUnrollingFinder finder = new LoopUnrollingFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).hasSize(1);
        assertThat(refactorings.get(0)).isInstanceOf(LoopUnrolling.class);
    }

    @Test
    public void testLoopUnrollingRefactoring() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/unrollableLoop.json");
        Script script = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        StmtList stmtList = script.getStmtList();
        RepeatTimesStmt loop = (RepeatTimesStmt) stmtList.getStmts().stream().filter(s -> s instanceof RepeatTimesStmt).findFirst().get();
        int times = (int)((NumberLiteral) loop.getTimes()).getValue();

        LoopUnrolling refactoring = new LoopUnrolling(loop, times);
        Program refactored = refactoring.apply(program);

        Script refactoredScript = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        StmtList refactoredStmtList = refactoredScript.getStmtList();
        assertThat(refactoredStmtList.getNumberOfStatements()).isEqualTo(stmtList.getNumberOfStatements() - 1 + times * loop.getStmtList().getNumberOfStatements());
    }
}
