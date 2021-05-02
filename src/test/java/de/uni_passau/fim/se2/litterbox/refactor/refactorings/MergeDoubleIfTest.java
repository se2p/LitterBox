package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MergeDoubleIfTest {

    @Test
    void applyTest() {
        File testFile = new File("src/test/testprojects/testdoublestmts.sb3");
        Program program = null;
        try {
            program = new Scratch3Parser().parseFile(testFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(program);

        Script script = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        List<Stmt> stmtList = script.getStmtList().getStmts();
        IfThenStmt if1 = null;
        IfThenStmt if2 = null;
        for (Stmt stmt :stmtList) {
            if (stmt instanceof IfThenStmt) {
                if (if1 == null) {
                    if1 = (IfThenStmt) stmt;
                } else {
                    if2 = (IfThenStmt) stmt;
                }
            }
        }
        assertNotNull(if1);
        assertNotNull(if2);

        MergeDoubleIf refactoring = new MergeDoubleIf(if1, if2);
        Program refactored = refactoring.apply(program);

        Script refactoredScript = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        List<Stmt> refactoredStmtList = refactoredScript.getStmtList().getStmts();
        assertEquals(1, refactoredStmtList.size());
        Stmt stmt = refactoredStmtList.get(0);
        assertTrue(stmt instanceof IfThenStmt);
    }

    @Test
    void getNameTest() {
        MergeDoubleIf refactoring = new MergeDoubleIf(mock(IfThenStmt.class), mock(IfThenStmt.class));
        assertEquals("merge_double_if", refactoring.getName());
    }

    @Test
    void toStringTest() {
        IfThenStmt ifStmt = new IfThenStmt(mock(BoolExpr.class), mock(StmtList.class), mock(BlockMetadata.class));
        MergeDoubleIf refactoring = new MergeDoubleIf(ifStmt, ifStmt);
        assertEquals("merge_double_if(IfThenStmt, IfThenStmt)", refactoring.toString());
    }

    @Test
    void testEqualOfRefactorings() {
        IfThenStmt ifStmt1 = mock(IfThenStmt.class);
        IfThenStmt ifStmt2 = mock(IfThenStmt.class);
        IfThenStmt ifStmt3 = mock(IfThenStmt.class);

        MergeDoubleIf refactoring1 = new MergeDoubleIf(ifStmt1, ifStmt2);
        MergeDoubleIf refactoring2 = new MergeDoubleIf(ifStmt1, ifStmt2);
        MergeDoubleIf refactoring3 = new MergeDoubleIf(ifStmt1, ifStmt3);

        assertEquals(refactoring1, refactoring1);
        assertEquals(refactoring1, refactoring2);
        assertNotEquals(refactoring1, refactoring3);
    }

    @Test
    void testHashCodeOfRefactorings() {
        IfThenStmt ifStmt1 = mock(IfThenStmt.class);
        IfThenStmt ifStmt2 = mock(IfThenStmt.class);
        IfThenStmt ifStmt3 = mock(IfThenStmt.class);

        MergeDoubleIf refactoring1 = new MergeDoubleIf(ifStmt1, ifStmt2);
        MergeDoubleIf refactoring2 = new MergeDoubleIf(ifStmt1, ifStmt2);
        MergeDoubleIf refactoring3 = new MergeDoubleIf(ifStmt1, ifStmt3);

        assertEquals(refactoring1.hashCode(), refactoring1.hashCode());
        assertEquals(refactoring1.hashCode(), refactoring2.hashCode());
        assertNotEquals(refactoring1.hashCode(), refactoring3.hashCode());
    }
}
