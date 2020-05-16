package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.DataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.DataExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TopLevelMetadataTest {
    private static ObjectMapper mapper = new ObjectMapper();
    private static JsonNode prog;

    @BeforeAll
    public static void setUp() throws IOException {
        File f = new File("./src/test/fixtures/metadata/blockMeta.json");
        prog = mapper.readTree(f);
    }


    @Test
    public void testVariablesProgram() throws ParsingException {
        Program program = ProgramParser.parseProgram("Test", prog);
        List<Script> scripts = program.getActorDefinitionList().getDefintions().get(1).getScripts().getScriptList();
        List<Stmt> stmtList = scripts.get(0).getStmtList().getStmts();
        Assertions.assertEquals(ExpressionStmt.class, stmtList.get(0).getClass());
        Expression expr = ((ExpressionStmt) stmtList.get(0)).getExpression();
        Assertions.assertEquals(Qualified.class, expr.getClass());
        DataExpr data = ((Qualified) expr).getSecond();
        Assertions.assertEquals(Variable.class, data.getClass());
        Assertions.assertEquals(DataBlockMetadata.class, data.getMetadata().getClass());
        DataBlockMetadata meta = (DataBlockMetadata) data.getMetadata();
        Assertions.assertEquals( 471, meta.getX());
    }
}
