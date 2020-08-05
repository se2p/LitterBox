/*
 * Copyright (C) 2020 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.truth.Truth;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.type.BooleanType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.StringType;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProcDefinitionParserTest {

    private static JsonNode project;

    @BeforeAll
    public static void setup() {
        String path = "src/test/fixtures/customBlocks.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            project = objectMapper.readTree(file);
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void testNoInputTest() {
        try {
            Program program = ProgramParser.parseProgram("CustomBlockTest", project);
            final List<ActorDefinition> defintions = program.getActorDefinitionList().getDefinitions();
            final List<ProcedureDefinition> list = defintions.get(1).getProcedureDefinitionList().getList();
            final String actorName = defintions.get(1).getIdent().getName();
            Truth.assertThat(list.get(0)).isInstanceOf(ProcedureDefinition.class);
            Assertions.assertEquals("BlockNoInputs",
                    program.getProcedureMapping().getProcedures().get(actorName).get(list.get(0).getIdent()).getName());
            Assertions.assertEquals(0,
                    program.getProcedureMapping().getProcedures().get(actorName).get(list.get(0).getIdent()).getArguments().length);
            Assertions.assertEquals(0, list.get(0).getParameterDefinitionList().getParameterDefinitions().size());
            Assertions.assertEquals(3, list.get(0).getStmtList().getStmts().size());
        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testInputTest() {
        try {
            Program program = ProgramParser.parseProgram("CustomBlockTest", project);
            final List<ActorDefinition> defintions = program.getActorDefinitionList().getDefinitions();
            final List<ProcedureDefinition> list = defintions.get(1).getProcedureDefinitionList().getList();
            final String actorName = defintions.get(1).getIdent().getName();

            Truth.assertThat(list.get(1)).isInstanceOf(ProcedureDefinition.class);
            ProcedureInfo procedureInfo =
                    program.getProcedureMapping().getProcedures().get(actorName).get(list.get(1).getIdent());
            Assertions.assertEquals("BlockWithInputs %s %b",
                    procedureInfo.getName());
            Assertions.assertEquals(2,
                    procedureInfo.getArguments().length);
            Assertions.assertEquals("NumInput",
                    procedureInfo.getArguments()[0].getName());
            Assertions.assertEquals("Boolean",
                    procedureInfo.getArguments()[1].getName());

            Truth.assertThat(procedureInfo.getArguments()[0].getType()).isInstanceOf(StringType.class);
            Truth.assertThat(procedureInfo.getArguments()[1].getType()).isInstanceOf(BooleanType.class);
            Assertions.assertEquals(3, list.get(1).getStmtList().getStmts().size());
            Assertions.assertEquals(procedureInfo.getArguments()[1].getName(),
                    list.get(1).getParameterDefinitionList().getParameterDefinitions().get(1).getIdent().getName());
            Assertions.assertEquals(procedureInfo.getArguments()[0].getName(),
                    list.get(1).getParameterDefinitionList().getParameterDefinitions().get(0).getIdent().getName());
            Assertions.assertEquals(NonDataBlockMetadata.class,
                    list.get(1).getParameterDefinitionList().getParameterDefinitions().get(0).getMetadata().getClass());
            Assertions.assertEquals(procedureInfo.getArguments()[1].getType(),
                    list.get(1).getParameterDefinitionList().getParameterDefinitions().get(1).getType());
            Assertions.assertEquals(procedureInfo.getArguments()[0].getType(),
                    list.get(1).getParameterDefinitionList().getParameterDefinitions().get(0).getType());
            Assertions.assertTrue(list.get(1).getStmtList().getStmts().get(0) instanceof MoveSteps);
            Truth.assertThat(((MoveSteps) list.get(1).getStmtList().getStmts().get(0)).getSteps()).isInstanceOf(AsNumber.class);
            Assertions.assertEquals("NumInput",
                    ((Parameter) ((AsNumber) ((MoveSteps) list.get(1).getStmtList().getStmts().get(0)).getSteps()).getOperand1()).getName().getName());
        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testMalformated() {
        File f = new File("./src/test/fixtures/bugpattern/malformatedProc.json");
        ObjectMapper mapper = new ObjectMapper();
        Exception exception = assertThrows(ParsingException.class, () -> {
            ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        });
        String expectedMessage = "Argument id has no corresponding input";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }
}
