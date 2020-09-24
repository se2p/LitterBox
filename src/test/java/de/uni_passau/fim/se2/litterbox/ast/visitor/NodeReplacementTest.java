package de.uni_passau.fim.se2.litterbox.ast.visitor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.TurnLeft;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NodeReplacementTest {
    @Test
    public void testDeletion() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/motionblocks.json");

        MoveSteps target = (MoveSteps)program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0).getStmtList().getStmts().get(0);
        Stmt replacement = new TurnLeft(target.getSteps(), target.getMetadata());

        NodeReplacementVisitor replacementVisitor = new NodeReplacementVisitor(target, replacement);
        Program programCopy = replacementVisitor.apply(program);

        StmtList statements1 = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0).getStmtList();
        StmtList statements2 = programCopy.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0).getStmtList();
        assertEquals(statements1.getStmts().size(), statements2.getStmts().size());
        assertEquals(target, statements1.getStmts().get(0));
        assertEquals(replacement, statements2.getStmts().get(0));
    }

    // TODO: We should really put all these clones in a common super/helper class for all unit tests...
    private Program getAST(String fileName) throws IOException, ParsingException {
        File file = new File(fileName);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode project = objectMapper.readTree(file);
        Program program = ProgramParser.parseProgram("TestProgram", project);
        return program;
    }
}
