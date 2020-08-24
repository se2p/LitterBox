package de.uni_passau.fim.se2.litterbox.ast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.UnspecifiedStmt;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

public class InvalidOpCodesTest {

    private static JsonNode project;

    @BeforeAll
    public static void setup() {
        String path = "src/test/fixtures/invalidOPCodes.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            project = objectMapper.readTree(file);
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void testInvalidOpCodes() throws ParsingException {
        // This project only contains invalid opcodes, thus the programs actor should not have any scripts
        Program program = ProgramParser.parseProgram("InvalidOpCodes", project);
        ActorDefinition sprite = program.getActorDefinitionList().getDefinitions().get(1);
        for (Script s : sprite.getScripts().getScriptList()) {
            Stmt stmt = s.getStmtList().getStmts().get(0);
            assertTrue("Sprite may not contain any stmts except Unspecifiedstmt", stmt instanceof UnspecifiedStmt);
        }
    }
}
