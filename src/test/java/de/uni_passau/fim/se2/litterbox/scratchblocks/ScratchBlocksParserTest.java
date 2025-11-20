/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.scratchblocks;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Message;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ScratchBlocksParserTest implements JsonTest {
    @Test
    void testAddNewScriptToEmptyProject() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        Assertions.assertEquals(0, program.getActorDefinitionList().getDefinitions().get(1).getScripts().getSize());
        ScratchBlocksParser parser = new ScratchBlocksParser();
        Program newProgram = parser.extendProject(program, "Sprite1", "when green flag clicked\nmove (10) steps\n");
        Assertions.assertEquals(1, newProgram.getActorDefinitionList().getDefinitions().get(1).getScripts().getSize());
    }

    @Test
    void testAddNewScriptsToEmptyProject() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        Assertions.assertEquals(0, program.getActorDefinitionList().getDefinitions().get(1).getScripts().getSize());
        ScratchBlocksParser parser = new ScratchBlocksParser();
        Program newProgram = parser.extendProject(program, "Sprite1", "when green flag clicked\nmove (10) steps\n\nsay ([backdrop # v] of (Stage v))\n");
        Assertions.assertEquals(2, newProgram.getActorDefinitionList().getDefinitions().get(1).getScripts().getSize());
        Script script = newProgram.getActorDefinitionList().getDefinitions().get(1).getScripts().getScript(1);
        Assertions.assertInstanceOf(Never.class, script.getEvent());
        Assertions.assertEquals(1, script.getStmtList().getStmts().size());
        Assertions.assertInstanceOf(Say.class, script.getStmtList().getStatement(0));
        Say say = (Say) script.getStmtList().getStatement(0);
        Assertions.assertInstanceOf(AttributeOf.class, say.getString());
    }

    @Test
    void testAddExistingVariableToProject() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        Assertions.assertEquals(2, program.getSymbolTable().getVariables().size());
        ScratchBlocksParser parser = new ScratchBlocksParser();
        Program newProgram = parser.extendProject(program, "Sprite1", "when green flag clicked\nmove (SpriteLocalVariable) steps\n");
        Assertions.assertEquals(1, newProgram.getActorDefinitionList().getDefinitions().get(1).getScripts().getSize());
        Script script = newProgram.getActorDefinitionList().getDefinitions().get(1).getScripts().getScript(0);
        Assertions.assertInstanceOf(MoveSteps.class, script.getStmtList().getStatement(0));
        MoveSteps moveSteps = (MoveSteps) script.getStmtList().getStatement(0);
        Assertions.assertInstanceOf(AsNumber.class, moveSteps.getSteps());
        Assertions.assertInstanceOf(Qualified.class, ((AsNumber) moveSteps.getSteps()).getOperand1());
        Assertions.assertEquals(2, newProgram.getSymbolTable().getVariables().size());
    }

    @Test
    void testAddNewVariableToProject() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        Assertions.assertEquals(2, program.getSymbolTable().getVariables().size());
        Assertions.assertEquals(11, program.getActorDefinitionList().getDefinitions().get(1).getSetStmtList().getStmts().size());
        ScratchBlocksParser parser = new ScratchBlocksParser();
        Program newProgram = parser.extendProject(program, "Sprite1", "when green flag clicked\nmove (NewSpriteVariable) steps\n");
        Assertions.assertEquals(1, newProgram.getActorDefinitionList().getDefinitions().get(1).getScripts().getSize());
        Script script = newProgram.getActorDefinitionList().getDefinitions().get(1).getScripts().getScript(0);
        Assertions.assertInstanceOf(MoveSteps.class, script.getStmtList().getStatement(0));
        MoveSteps moveSteps = (MoveSteps) script.getStmtList().getStatement(0);
        Assertions.assertInstanceOf(AsNumber.class, moveSteps.getSteps());
        Assertions.assertInstanceOf(Qualified.class, ((AsNumber) moveSteps.getSteps()).getOperand1());
        Assertions.assertEquals(3, newProgram.getSymbolTable().getVariables().size());
        Assertions.assertEquals(12, newProgram.getActorDefinitionList().getDefinitions().get(1).getSetStmtList().getStmts().size());
    }

    @Test
    void testAddNewVariableExprToProject() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        Assertions.assertEquals(2, program.getSymbolTable().getVariables().size());
        Assertions.assertEquals(11, program.getActorDefinitionList().getDefinitions().get(1).getSetStmtList().getStmts().size());
        ScratchBlocksParser parser = new ScratchBlocksParser();
        Program newProgram = parser.extendProject(program, "Sprite1", "(len)\n");
        Assertions.assertEquals(3, newProgram.getSymbolTable().getVariables().size());
        Assertions.assertEquals(12, newProgram.getActorDefinitionList().getDefinitions().get(1).getSetStmtList().getStmts().size());
    }

    @Test
    void testAddNewListToProject() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        Assertions.assertEquals(1, program.getSymbolTable().getLists().size());
        ScratchBlocksParser parser = new ScratchBlocksParser();
        Program newProgram = parser.extendProject(program, "Sprite1", "when green flag clicked\nmove (NewSpriteList :: list) steps\n");
        Assertions.assertEquals(1, newProgram.getActorDefinitionList().getDefinitions().get(1).getScripts().getSize());
        Script script = newProgram.getActorDefinitionList().getDefinitions().get(1).getScripts().getScript(0);
        Assertions.assertInstanceOf(MoveSteps.class, script.getStmtList().getStatement(0));
        MoveSteps moveSteps = (MoveSteps) script.getStmtList().getStatement(0);
        Assertions.assertInstanceOf(AsNumber.class, moveSteps.getSteps());
        Assertions.assertInstanceOf(Qualified.class, ((AsNumber) moveSteps.getSteps()).getOperand1());
        Assertions.assertEquals(2, newProgram.getSymbolTable().getLists().size());
    }

    @Test
    void testAddNewMessageToProject() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        Assertions.assertEquals(0, program.getSymbolTable().getMessages().size());
        ScratchBlocksParser parser = new ScratchBlocksParser();
        Program newProgram = parser.extendProject(program, "Sprite1", "when green flag clicked\nbroadcast (newMessage v)\n");
        Assertions.assertEquals(1, newProgram.getSymbolTable().getMessages().size());
        Script script = newProgram.getActorDefinitionList().getDefinitions().get(1).getScripts().getScript(0);
        Assertions.assertInstanceOf(Broadcast.class, script.getStmtList().getStatement(0));
        Broadcast broadcast = (Broadcast) script.getStmtList().getStatement(0);
        Message message = broadcast.getMessage();
        Assertions.assertEquals("newMessage", ((StringLiteral) message.getMessage()).getText());
        Assertions.assertTrue(newProgram.getSymbolTable().getMessage("newMessage").isPresent());
    }

    @Test
    void testAddNewProcedureToProject() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        Assertions.assertEquals(0, program.getProcedureMapping().getProcedures().size());
        ScratchBlocksParser parser = new ScratchBlocksParser();
        Program newProgram = parser.extendProject(program, "Sprite1", "define test\n");
        Assertions.assertEquals(1, newProgram.getProcedureMapping().getProcedures().size());
        Assertions.assertEquals(1, newProgram.getActorDefinitionList().getDefinitions().get(1).getProcedureDefinitionList().getList().size());
    }

    @Test
    void testAddNewProcedureWithParamToProject() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        Assertions.assertEquals(0, program.getProcedureMapping().getProcedures().size());
        ScratchBlocksParser parser = new ScratchBlocksParser();
        Program newProgram = parser.extendProject(program, "Sprite1", "define test (schritte) steps <evtl> do\n");
        Assertions.assertEquals(1, newProgram.getProcedureMapping().getProcedures().size());
        Assertions.assertEquals(1, newProgram.getActorDefinitionList().getDefinitions().get(1).getProcedureDefinitionList().getList().size());
        ProcedureDefinition procedureDefinition = newProgram.getActorDefinitionList().getDefinitions().get(1).getProcedureDefinitionList().getList().getFirst();
        Assertions.assertEquals(2, procedureDefinition.getParameterDefinitionList().getParameterDefinitions().size());
    }

    @Test
    void testCommentsInVariousPlaces() {
        ScratchBlocksParser parser = new ScratchBlocksParser();
        String input = "when green flag clicked\n" +
                "// comment after hat\n" +
                "move (10) steps\n" +
                "// comment between blocks\n" +
                "move (20) steps\n" +
                "// comment at end\n";
        Script script = parser.parseScript("Stage", input);
        Assertions.assertNotNull(script);
        Assertions.assertInstanceOf(GreenFlag.class, script.getEvent());
        Assertions.assertEquals(2, script.getStmtList().getStmts().size());
        Assertions.assertInstanceOf(MoveSteps.class, script.getStmtList().getStatement(0));
        Assertions.assertInstanceOf(MoveSteps.class, script.getStmtList().getStatement(1));
    }

    @Test
    void testCommentBetweenHatAndStack() {
        ScratchBlocksParser parser = new ScratchBlocksParser();
        String input = "when green flag clicked\n// comment\nsay (foo)\n";
        Script script = parser.parseScript("Stage", input);

        Assertions.assertNotNull(script);
        Assertions.assertInstanceOf(GreenFlag.class, script.getEvent(), "Event should be GreenFlag");
        Assertions.assertEquals(1, script.getStmtList().getStmts().size(), "Should have 1 statement");
        Assertions.assertInstanceOf(Say.class, script.getStmtList().getStatement(0), "Statement should be Say");
    }
}
