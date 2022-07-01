package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class OnlyEmptyCodeCloneVisitorTest implements JsonTest {

    @Test
    public void testForever() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/visitors/foreverWithSay.json");
        OnlyEmptyCodeCloneVisitor vis = new OnlyEmptyCodeCloneVisitor();
        Script script = vis.apply(program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScript(0));
        Assertions.assertTrue(script.getStmtList().getStmts().get(0) instanceof RepeatForeverStmt);
        RepeatForeverStmt forever = (RepeatForeverStmt) script.getStmtList().getStmts().get(0);
        Assertions.assertTrue(forever.getStmtList().getStmts().isEmpty());
    }
}
