package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MergeDoubleIfTest implements JsonTest {

    private Program program;
    private IfThenStmt if0;
    private IfThenStmt if1;
    private IfThenStmt if2;
    private Refactoring refactoring;

    @BeforeEach
    public void setUp() throws ParsingException, IOException {
        program = getAST("src/test/fixtures/refactoring/simple-ifs.json");
        Script script = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        List<Stmt> stmtList = script.getStmtList().getStmts();
        RepeatForeverStmt forever = (RepeatForeverStmt) stmtList.get(0);
        if0 = (IfThenStmt) forever.getStmtList().getStmts().get(0);
        if1 = (IfThenStmt) forever.getStmtList().getStmts().get(1);
        if2 = (IfThenStmt) forever.getStmtList().getStmts().get(2);
        refactoring = new MergeDoubleIf(if1, if2);
    }

    @Test
    void applyTest() {
        File testFile = new File("src/test/fixtures/refactoring/testdoublestmts.json");
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
    void simpleIfsTest() {
        Program refactored = refactoring.apply(program);

        ScratchBlocksVisitor scratchBlocksVisitor = new ScratchBlocksVisitor();
        refactored.accept(scratchBlocksVisitor);
        String scratchBlocks = scratchBlocksVisitor.getScratchBlocks();
        assertThat(scratchBlocks).isEqualTo(
                "when green flag clicked\n"
                        + "forever \n"
                        + "if <touching (mouse-pointer v) ?> then\n"
                        + "move (10) steps\n"
                        + "end\n"
                        + "if <touching (edge v) ?> then\n"
                        + "move (5) steps\n"
                        + "move (2) steps\n"
                        + "end\n"
                        + "end\n");
    }

    @Test
    void getNameTest() {
        assertEquals("merge_double_if", refactoring.getName());
    }

    @Test
    void toStringTest() {
        assertThat(refactoring.toString()).isEqualTo("merge_double_if\n"
                + "Replaced ifs:\n"
                + "\n"
                + "if <touching (edge v) ?> then\n"
                + "move (5) steps\n"
                + "end\n"
                + "\n"
                + "if <touching (edge v) ?> then\n"
                + "move (2) steps\n"
                + "end\n"
                + "\n"
                + "Replacement:\n"
                + "\n"
                + "if <touching (edge v) ?> then\n"
                + "move (5) steps\n"
                + "move (2) steps\n"
                + "end\n");
    }

    @Test
    void testEqualOfRefactorings() {
        Refactoring equalRefactoring = new MergeDoubleIf(if1, if2);
        Refactoring nonEqualRefactoring = new MergeDoubleIf(if0, if1);

        assertEquals(refactoring, refactoring);
        assertEquals(refactoring, equalRefactoring);
        assertNotEquals(refactoring, nonEqualRefactoring);
    }

    @Test
    void testHashCodeOfRefactorings() {
        Refactoring equalRefactoring = new MergeDoubleIf(if1, if2);
        Refactoring nonEqualRefactoring = new MergeDoubleIf(if0, if1);

        assertEquals(refactoring.hashCode(), refactoring.hashCode());
        assertEquals(refactoring.hashCode(), equalRefactoring.hashCode());
        assertNotEquals(refactoring.hashCode(), nonEqualRefactoring.hashCode());
    }

    @Test
    public void testASTStructure() {
        Program refactored = refactoring.apply(program);
        CloneVisitor visitor = new CloneVisitor();
        Program clone = visitor.apply(refactored);
        assertEquals(refactored, clone);
    }
}
