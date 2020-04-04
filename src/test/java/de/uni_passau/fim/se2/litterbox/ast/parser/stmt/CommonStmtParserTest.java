/*
 * Copyright (C) 2019 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.truth.Truth;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeAttributeBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ResetTimer;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetAttributeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.StopOtherScriptsInSprite;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static junit.framework.TestCase.fail;

public class CommonStmtParserTest {

    private static JsonNode project;

    @BeforeAll
    public static void setup() {
        String path = "src/test/fixtures/stmtParser/commonStmts.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            project = objectMapper.readTree(file);
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void testProgramStructure() {
        try {
            Program program = ProgramParser.parseProgram("CommonStmts", project);
            ActorDefinitionList list = program.getActorDefinitionList();
            Truth.assertThat(list.getDefintions().size()).isEqualTo(2);
        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testStmtsInSprite() {
        try {
            Program program = ProgramParser.parseProgram("CommonStmts", project);
            ActorDefinitionList list = program.getActorDefinitionList();
            ActorDefinition sprite = list.getDefintions().get(1);

            Script script = sprite.getScripts().getScriptList().get(0);
            List<Stmt> listOfStmt = script.getStmtList().getStmts();

            Truth.assertThat(listOfStmt.get(0).getClass()).isEqualTo(WaitSeconds.class);
            Truth.assertThat(listOfStmt.get(1).getClass()).isEqualTo(WaitUntil.class);
            Truth.assertThat(listOfStmt.get(2).getClass()).isEqualTo(StopOtherScriptsInSprite.class);
            Truth.assertThat(listOfStmt.get(3).getClass()).isEqualTo(CreateCloneOf.class);
            Truth.assertThat(listOfStmt.get(4).getClass()).isEqualTo(Broadcast.class);
            Truth.assertThat(listOfStmt.get(5).getClass()).isEqualTo(BroadcastAndWait.class);
            Truth.assertThat(listOfStmt.get(6).getClass()).isEqualTo(ResetTimer.class);
            Truth.assertThat(listOfStmt.get(7).getClass()).isEqualTo(ChangeVariableBy.class);
            Truth.assertThat(listOfStmt.get(8).getClass()).isEqualTo(ChangeAttributeBy.class);
            Truth.assertThat(listOfStmt.get(9).getClass()).isEqualTo(SetAttributeTo.class);
            Truth.assertThat(listOfStmt.get(10).getClass()).isEqualTo(ChangeAttributeBy.class);
            Truth.assertThat(listOfStmt.get(11).getClass()).isEqualTo(DeleteClone.class);
        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }
}