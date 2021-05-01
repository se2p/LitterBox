package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.ControlStmt;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

public class DeleteControlBlockTest {

    @Test
    public void applyTest() {
        File testFile = new File("src/test/testprojects/testdummyrefactorings.sb3");
        Program program = null;
        try {
            program = new Scratch3Parser().parseFile(testFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(program);

        Script script = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        List<Stmt> stmtList = script.getStmtList().getStmts();
        ControlStmt controlStmt = null;
        for (Stmt stmt : stmtList) {
            if (stmt instanceof ControlStmt) {
                controlStmt = (ControlStmt) stmt;
            }
        }
        assertNotNull(controlStmt);

        DeleteControlBlock refactoring = new DeleteControlBlock(script);
        Program refactored = refactoring.apply(program);

        Script refactoredScript = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        List<Stmt> refactoredStmtList = refactoredScript.getStmtList().getStmts();
        assertFalse(refactoredStmtList.contains(controlStmt));
    }

    @Test
    public void getNameTest() {
        DeleteControlBlock refactoring = new DeleteControlBlock(mock(Script.class));
        assertEquals("delete_control_block", refactoring.getName());
    }

    @Test
    public void toStringTest() {
        Script script = new Script(mock(Event.class), mock(StmtList.class));
        DeleteControlBlock refactoring = new DeleteControlBlock(script);
        assertEquals("delete_control_block(Script)", refactoring.toString());
    }
}
