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
package scratch.ast.parser.stmt;

import static junit.framework.TestCase.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.truth.Truth;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scratch.ast.ParsingException;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.ActorDefinitionList;
import scratch.ast.model.Program;
import scratch.ast.model.Script;
import scratch.ast.model.elementchoice.WithId;
import scratch.ast.model.statement.Stmt;
import scratch.ast.model.statement.actorsound.ClearSoundEffects;
import scratch.ast.model.statement.actorsound.PlaySoundUntilDone;
import scratch.ast.model.statement.actorsound.StartSound;
import scratch.ast.model.statement.actorsound.StopAllSounds;
import scratch.ast.model.statement.termination.StopAll;
import scratch.ast.model.variable.StrId;
import scratch.ast.parser.ProgramParser;

public class ActorSoundStmtParserTest {

    private static JsonNode project;

    @BeforeAll
    public static void setup() {
        String path = "src/test/fixtures/stmtParser/actorSoundStmts.json";
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
            Program program = ProgramParser.parseProgram("ActorSoundStmts", project);
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
            Program program = ProgramParser.parseProgram("ActorLookStmts", project);
            ActorDefinitionList list = program.getActorDefinitionList();
            ActorDefinition sprite = list.getDefintions().get(1);

            Script script = sprite.getScripts().getScriptList().get(0);
            List<Stmt> listOfStmt = script.getStmtList().getStmts().getListOfStmt();

            Truth.assertThat(listOfStmt.get(0).getClass()).isEqualTo(PlaySoundUntilDone.class);
            Truth.assertThat(listOfStmt.get(1).getClass()).isEqualTo(StartSound.class);
            Truth.assertThat(listOfStmt.get(2).getClass()).isEqualTo(ClearSoundEffects.class);
            Truth.assertThat(listOfStmt.get(3).getClass()).isEqualTo(StopAllSounds.class);
            Truth.assertThat(listOfStmt.get(4).getClass()).isEqualTo(StopAll.class);

        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testPlaySoundTilDone() {
        try {
            Program program = ProgramParser.parseProgram("ActorLookStmts", project);
            ActorDefinitionList list = program.getActorDefinitionList();
            ActorDefinition sprite = list.getDefintions().get(1);

            Script script = sprite.getScripts().getScriptList().get(0);
            List<Stmt> listOfStmt = script.getStmtList().getStmts().getListOfStmt();

            Truth.assertThat(listOfStmt.get(0).getClass()).isEqualTo(PlaySoundUntilDone.class);

            PlaySoundUntilDone playSoundUntilDone = (PlaySoundUntilDone) listOfStmt.get(0);
            Truth.assertThat(((StrId) ((WithId) playSoundUntilDone.getElementChoice()).getStringExpr()).getValue())
                .isEqualTo("Meow");

        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testStartSound() {
        try {
            Program program = ProgramParser.parseProgram("ActorLookStmts", project);
            ActorDefinitionList list = program.getActorDefinitionList();
            ActorDefinition sprite = list.getDefintions().get(1);

            Script script = sprite.getScripts().getScriptList().get(0);
            List<Stmt> listOfStmt = script.getStmtList().getStmts().getListOfStmt();

            Truth.assertThat(listOfStmt.get(0).getClass()).isEqualTo(PlaySoundUntilDone.class);

            StartSound startSound = (StartSound) listOfStmt.get(1);
            Truth.assertThat(((StrId) ((WithId) startSound.getElementChoice()).getStringExpr()).getValue())
                .isEqualTo("Meow");

        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

}