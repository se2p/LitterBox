/*
 * Copyright (C) 2019-2022 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import de.uni_passau.fim.se2.litterbox.JsonTest;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.VAR_PRIMITIVE;

public class TopLevelMetadataTest implements JsonTest {
    private static Program program;

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {
        program = JsonTest.parseProgram("./src/test/fixtures/metadata/blockMeta.json");
    }

    @Test
    public void testVariablesProgram() {
        List<Script> scripts = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList();
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
    }

    @Test
    public void testProcedureProgram() {
        ProcedureDefinition def =
                program.getActorDefinitionList().getDefinitions().get(1).getProcedureDefinitionList().getList().get(0);
        ProcedureMetadata meta = (ProcedureMetadata) def.getMetadata();
        Assertions.assertEquals(TopNonDataBlockMetadata.class, meta.getDefinition().getClass());
        TopNonDataBlockMetadata defMet = (TopNonDataBlockMetadata) meta.getDefinition();
        Assertions.assertEquals(NoMutationMetadata.class, defMet.getMutation().getClass());
        Assertions.assertEquals(56, defMet.getXPos());
        Assertions.assertEquals(184, defMet.getYPos());
        Assertions.assertEquals(NonDataBlockMetadata.class, meta.getPrototype().getClass());
        NonDataBlockMetadata protoMet = (NonDataBlockMetadata) meta.getPrototype();
        Assertions.assertEquals(ProcedureMutationMetadata.class, protoMet.getMutation().getClass());
    }
}
