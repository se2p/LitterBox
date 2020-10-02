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

import com.google.common.truth.Truth;
import de.uni_passau.fim.se2.litterbox.JsonTest;
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
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class ActorLookStmtParserTest implements JsonTest {

    @Test
    public void testProgramStructure() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/stmtParser/actorLookStmts.json");
        ActorDefinitionList list = program.getActorDefinitionList();
        Truth.assertThat(list.getDefinitions().size()).isEqualTo(2);
    }

    @Test
    public void testStmtsInSprite() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/stmtParser/actorLookStmts.json");
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
    }

    @Test
    public void testAskAndWaitStmt() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/stmtParser/actorLookStmts.json");
        ActorDefinitionList list = program.getActorDefinitionList();
        ActorDefinition sprite = list.getDefinitions().get(1);

        Script script = sprite.getScripts().getScriptList().get(0);
        List<Stmt> listOfStmt = script.getStmtList().getStmts();

        Stmt askAndWaitStmt = listOfStmt.get(0);
        Truth.assertThat(askAndWaitStmt.getClass()).isEqualTo(AskAndWait.class);
        Truth.assertThat(((StringLiteral) ((AskAndWait) askAndWaitStmt).getQuestion()).getText())
                .isEqualTo("What's your name?");
    }

    @Test
    public void testSwitchBackdrop() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/stmtParser/actorLookStmts.json");
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
    }

    @Test
    public void testSwitchBackdropMetadata() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/stmtParser/actorLookStmts.json");
        ActorDefinitionList list = program.getActorDefinitionList();
        ActorDefinition sprite = list.getDefinitions().get(1);

        Script script = sprite.getScripts().getScriptList().get(0);
        List<Stmt> listOfStmt = script.getStmtList().getStmts();

        Stmt switchBackdrop = listOfStmt.get(1);
        BlockMetadata metadata = ((SwitchBackdrop) switchBackdrop).getMetadata();
        Truth.assertThat(metadata).isInstanceOf(NonDataBlockMetadata.class);
    }

    @Test
    public void testShowHideVar() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/stmtParser/actorLookStmts.json");
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
    }

    @Test
    public void testShowHideList() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/stmtParser/actorLookStmts.json");
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
                .isEqualTo("List");
    }
}
