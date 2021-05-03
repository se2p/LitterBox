package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.ControlStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeleteControlBlockTest {

    @Test
    void applyTest() {
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

        DeleteControlBlock refactoring = new DeleteControlBlock(controlStmt);
        program = refactoring.apply(program);

        Script refactoredScript = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        List<Stmt> refactoredStmtList = refactoredScript.getStmtList().getStmts();
        assertFalse(refactoredStmtList.contains(controlStmt));
    }

    @Test
    void getNameTest() {
        DeleteControlBlock refactoring = new DeleteControlBlock(mock(IfThenStmt.class));
        assertEquals("delete_control_block", refactoring.getName());
    }

    @Test
    void toStringTest() {
        ControlStmt controlStmt = mock(IfThenStmt.class);
        when(controlStmt.getUniqueName()).thenReturn("ControlStatement");
        DeleteControlBlock refactoring = new DeleteControlBlock(controlStmt);
        assertEquals("delete_control_block(ControlStatement)", refactoring.toString());
    }
}
