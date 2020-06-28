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
package de.uni_passau.fim.se2.litterbox.ast.parser.stmt;

import static junit.framework.TestCase.fail;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.truth.Truth;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.ElementChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ActorLookStmtParserTest {

    private static JsonNode project;

    @BeforeAll
    public static void setup() {
        String path = "src/test/fixtures/stmtParser/actorLookStmts.json";
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
            Program program = ProgramParser.parseProgram("ActorLookStmts", project);
            ActorDefinitionList list = program.getActorDefinitionList();
            Truth.assertThat(list.getDefinitions().size()).isEqualTo(2);
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
            ActorDefinition sprite = list.getDefinitions().get(1);

            Script script = sprite.getScripts().getScriptList().get(0);
            List<Stmt> listOfStmt = script.getStmtList().getStmts();

            Truth.assertThat(listOfStmt.get(0).getClass()).isEqualTo(AskAndWait.class);
            Truth.assertThat(listOfStmt.get(1).getClass()).isEqualTo(SwitchBackdrop.class);
            Truth.assertThat(listOfStmt.get(2).getClass()).isEqualTo(ShowVariable.class);
            Truth.assertThat(listOfStmt.get(3).getClass()).isEqualTo(HideVariable.class);
            Truth.assertThat(listOfStmt.get(4).getClass()).isEqualTo(ShowList.class);
            Truth.assertThat(listOfStmt.get(5).getClass()).isEqualTo(HideList.class);
            Truth.assertThat(listOfStmt.get(6).getClass()).isEqualTo(ClearGraphicEffects.class);
            Truth.assertThat(listOfStmt.get(7).getClass()).isEqualTo(StopAll.class);
        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testAskAndWaitStmt() {
        try {
            Program program = ProgramParser.parseProgram("ActorLookStmts", project);
            ActorDefinitionList list = program.getActorDefinitionList();
            ActorDefinition sprite = list.getDefinitions().get(1);

            Script script = sprite.getScripts().getScriptList().get(0);
            List<Stmt> listOfStmt = script.getStmtList().getStmts();

            Stmt askAndWaitStmt = listOfStmt.get(0);
            Truth.assertThat(askAndWaitStmt.getClass()).isEqualTo(AskAndWait.class);
            Truth.assertThat(((StringLiteral) ((AskAndWait) askAndWaitStmt).getQuestion()).getText())
                    .isEqualTo("What's your name?");
        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testSwitchBackdrop() {
        try {
            Program program = ProgramParser.parseProgram("ActorLookStmts", project);
            ActorDefinitionList list = program.getActorDefinitionList();
            ActorDefinition sprite = list.getDefinitions().get(1);

            Script script = sprite.getScripts().getScriptList().get(0);
            List<Stmt> listOfStmt = script.getStmtList().getStmts();

            Stmt switchBackropStmt = listOfStmt.get(1);
            Truth.assertThat(switchBackropStmt.getClass()).isEqualTo(SwitchBackdrop.class);

            ElementChoice elementChoice = ((SwitchBackdrop) switchBackropStmt).getElementChoice();
            Expression expression = ((WithExpr) elementChoice).getExpression();
            StrId strid = (StrId) expression;

            Truth.assertThat(strid.getName()).isEqualTo("Baseball 1");
        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testShowHideVar() {
        try {
            Program program = ProgramParser.parseProgram("ActorLookStmts", project);
            ActorDefinitionList list = program.getActorDefinitionList();
            ActorDefinition sprite = list.getDefinitions().get(1);

            Script script = sprite.getScripts().getScriptList().get(0);
            List<Stmt> listOfStmt = script.getStmtList().getStmts();

            Stmt showVariable = listOfStmt.get(2);
            Truth.assertThat(showVariable.getClass()).isEqualTo(ShowVariable.class);
            Truth.assertThat(((Qualified) ((ShowVariable) showVariable).getIdentifier()).getFirst().getName())
                    .isEqualTo("Stage");
            Truth.assertThat(((Qualified) ((ShowVariable) showVariable).getIdentifier()).getSecond().getName().getName())
                    .isEqualTo("my variable");

            Stmt hideVariable = listOfStmt.get(3);
            Truth.assertThat(((Qualified) ((HideVariable) hideVariable).getIdentifier()).getFirst().getName())
                    .isEqualTo("Stage");
            Truth.assertThat(((Qualified) ((HideVariable) hideVariable).getIdentifier()).getSecond().getName().getName())
                    .isEqualTo("my variable");
        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testShowHideList() {
        try {
            Program program = ProgramParser.parseProgram("ActorLookStmts", project);
            ActorDefinitionList list = program.getActorDefinitionList();
            ActorDefinition sprite = list.getDefinitions().get(1);

            Script script = sprite.getScripts().getScriptList().get(0);
            List<Stmt> listOfStmt = script.getStmtList().getStmts();

            Stmt showVariable = listOfStmt.get(4);
            Truth.assertThat(showVariable.getClass()).isEqualTo(ShowList.class);
            Truth.assertThat(((Qualified) ((ShowList) showVariable).getIdentifier()).getFirst().getName())
                    .isEqualTo("Stage");
            Truth.assertThat(((Qualified) ((ShowList) showVariable).getIdentifier()).getSecond().getName().getName())
                    .isEqualTo("List");

            Stmt hideVariable = listOfStmt.get(5);
            Truth.assertThat(((Qualified) ((HideList) hideVariable).getIdentifier()).getFirst().getName())
                    .isEqualTo("Stage");
            Truth.assertThat(((Qualified) ((HideList) hideVariable).getIdentifier()).getSecond().getName().getName())
                    .isEqualTo( "List");
        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }
}