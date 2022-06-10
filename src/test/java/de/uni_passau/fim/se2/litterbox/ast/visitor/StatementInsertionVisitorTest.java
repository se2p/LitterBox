package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StatementInsertionVisitorTest implements JsonTest {

    @Test
    public void testInsertionScript() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/visitors/insertionBasis.json");

        Script parent = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScript(0);

        MoveSteps move = new MoveSteps(new NumberLiteral(10), new NoBlockMetadata());

        StatementInsertionVisitor visitor = new StatementInsertionVisitor(parent, 0, move);
        Program programCopy = visitor.apply(program);

        StmtList statements1 = programCopy.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0).getStmtList();
        assertEquals(parent.getStmtList().getStmts().size() + 1, statements1.getStmts().size());
        assertEquals(move, statements1.getStatement(0));
    }

    @Test
    public void testInsertionForevert() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/visitors/insertionBasis.json");

        RepeatForeverStmt parent = (RepeatForeverStmt) program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScript(0).getStmtList().getStatement(0);

        MoveSteps move = new MoveSteps(new NumberLiteral(10), new NoBlockMetadata());

        StatementInsertionVisitor visitor = new StatementInsertionVisitor(parent, 0, move);
        Program programCopy = visitor.apply(program);

        StmtList statements1 = ((RepeatForeverStmt) programCopy.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0).getStmtList().getStatement(0)).getStmtList();
        assertEquals(parent.getStmtList().getStmts().size() + 1, statements1.getStmts().size());
        assertEquals(move, statements1.getStatement(0));
    }
}
