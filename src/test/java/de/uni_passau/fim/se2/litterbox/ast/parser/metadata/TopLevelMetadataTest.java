package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProcedureMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.*;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
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

import static de.uni_passau.fim.se2.litterbox.ast.Constants.VAR_PRIMITIVE;

public class TopLevelMetadataTest {
    private static ObjectMapper mapper = new ObjectMapper();
    private static Program program;

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/metadata/blockMeta.json");
        JsonNode prog = mapper.readTree(f);
        program = ProgramParser.parseProgram("Test", prog);
    }


    @Test
    public void testVariablesProgram() {
        List<Script> scripts = program.getActorDefinitionList().getDefintions().get(1).getScripts().getScriptList();
        List<Stmt> stmtList = scripts.get(0).getStmtList().getStmts();
        Assertions.assertEquals(ExpressionStmt.class, stmtList.get(0).getClass());
        Expression expr = ((ExpressionStmt) stmtList.get(0)).getExpression();
        Assertions.assertEquals(Qualified.class, expr.getClass());
        DataExpr data = ((Qualified) expr).getSecond();
        Assertions.assertEquals(Variable.class, data.getClass());
        Assertions.assertEquals(DataBlockMetadata.class, data.getMetadata().getClass());
        DataBlockMetadata meta = (DataBlockMetadata) data.getMetadata();
        Assertions.assertEquals(471, meta.getX());
        Assertions.assertEquals(383, meta.getY());
        Assertions.assertEquals(VAR_PRIMITIVE, meta.getDataType());
    }

    @Test
    public void testProcedureProgram(){
        ProcedureDefinition def =
                program.getActorDefinitionList().getDefintions().get(1).getProcedureDefinitionList().getList().get(0);
        ProcedureMetadata meta = def.getMetadata();
        Assertions.assertEquals(TopNonDataBlockMetadata.class, meta.getDefinition().getClass());
        TopNonDataBlockMetadata defMet = (TopNonDataBlockMetadata) meta.getDefinition();
        Assertions.assertEquals(NoMutationMetadata.class, defMet.getMutation().getClass());
        Assertions.assertEquals(56, defMet.getxPos());
        Assertions.assertEquals(184, defMet.getyPos());
        Assertions.assertEquals(NonDataBlockMetadata.class, meta.getPrototype().getClass());
        NonDataBlockMetadata protoMet = (NonDataBlockMetadata) meta.getPrototype();
        Assertions.assertEquals(PrototypeMutationMetadata.class, protoMet.getMutation().getClass());
        PrototypeMutationMetadata mutationMetadata = (PrototypeMutationMetadata) protoMet.getMutation();
        Assertions.assertEquals("TestMethod %s", mutationMetadata.getProcCode());
        Assertions.assertEquals("[\"k~QZ.p5)uSGZZ]?@TWD$\"]", mutationMetadata.getArgumentIds());
    }
}
