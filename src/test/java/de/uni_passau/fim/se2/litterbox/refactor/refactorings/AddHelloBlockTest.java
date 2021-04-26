package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class AddHelloBlockTest {

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
        AddHelloBlock refactoring = new AddHelloBlock(script);
        refactoring.apply(program);

        Script refactoredScript = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        List<Stmt> refactoredStmtList = refactoredScript.getStmtList().getStmts();
        Stmt newBlock = refactoredStmtList.get(refactoredStmtList.size() - 1);
        assertTrue(newBlock instanceof Say);
        assertEquals(new StringLiteral("Hello!"), ((Say) newBlock).getString());
    }

    @Test
    public void getNameTest() {
        AddHelloBlock refactoring = new AddHelloBlock(mock(Script.class));
        assertEquals("add_hello_block", refactoring.getName());
    }

    @Test
    public void toStringTest() {
        Script script = new Script(mock(Event.class), mock(StmtList.class));
        AddHelloBlock refactoring = new AddHelloBlock(script);
        assertEquals("add_hello_block(Script)", refactoring.toString());
    }

}
