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
package de.uni_passau.fim.se2.litterbox.llm;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StageClicked;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Think;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class LlmResponseParserTest implements JsonTest {

    @Test
    void testAddVariableToExistingScript() throws ParsingException, IOException {
        String response = """
                scratch
                //Sprite: Sprite1
                //Script: V/6:G4i[HL#.bvM4XA|8
                when green flag clicked
                move (newVar) steps
                forever
                    if <key (space v) pressed?> then
                        turn right (15) degrees
                    end
                end
                """;
        Program program = getAST("./src/test/fixtures/playerSpriteMissingLoop.json");
        Assertions.assertEquals(1, program.getSymbolTable().getVariables().size());
        Assertions.assertEquals(9, program.getActorDefinitionList().getDefinitions().get(1).getSetStmtList().getStmts().size());
        LlmResponseParser responseParser = new LlmResponseParser();
        var parsedResponse = responseParser.parseLLMResponse(response);
        Program updatedProgram = responseParser.updateProgram(program, parsedResponse);
        Assertions.assertEquals(2, updatedProgram.getSymbolTable().getVariables().size());
        Assertions.assertEquals(10, updatedProgram.getActorDefinitionList().getDefinitions().get(1).getSetStmtList().getStmts().size());
    }

    @Test
    void testAddMessageToExistingScript() throws ParsingException, IOException {
        String response = """
                scratch
                //Sprite: Sprite1
                //Script: V/6:G4i[HL#.bvM4XA|8
                when green flag clicked
                broadcast (test v)
                forever
                    if <key (space v) pressed?> then
                        turn right (15) degrees
                    end
                end
                """;
        Program program = getAST("./src/test/fixtures/playerSpriteMissingLoop.json");
        Assertions.assertEquals(0, program.getSymbolTable().getMessages().size());
        LlmResponseParser responseParser = new LlmResponseParser();
        var parsedResponse = responseParser.parseLLMResponse(response);
        Program updatedProgram = responseParser.updateProgram(program, parsedResponse);
        Assertions.assertEquals(1, updatedProgram.getSymbolTable().getMessages().size());
        Assertions.assertTrue(updatedProgram.getSymbolTable().getMessage("test").isPresent());
    }

    @Test
    void testAddNewScript() throws ParsingException, IOException {
        String response = """
                scratch
                //Sprite: Sprite1
                //Script: newlyadded
                when green flag clicked
                say [test]
                """;
        Program program = getAST("./src/test/fixtures/playerSpriteMissingLoop.json");
        Assertions.assertEquals(1, program.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize());
        LlmResponseParser responseParser = new LlmResponseParser();
        var parsedResponse = responseParser.parseLLMResponse(response);
        Program updatedProgram = responseParser.updateProgram(program, parsedResponse);
        Assertions.assertEquals(2, updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize());
        List<Script> scripts = updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getScriptList();
        Script newScript = scripts.get(1);
        Assertions.assertEquals(1, newScript.getStmtList().getStmts().size());
        Assertions.assertInstanceOf(Say.class, newScript.getStmtList().getStmts().get(0));
    }

    @Test
    void testAddNewScriptsMultipleActors() throws ParsingException, IOException {
        String response = """
                scratch
                //Sprite: Stage
                //Script: newlyadded2
                when stage clicked
                think (new)

                //Sprite: Sprite1
                //Script: newlyadded
                when green flag clicked
                say [test]
                """;
        Program program = getAST("./src/test/fixtures/emptyProject.json");
        Assertions.assertEquals(0, program.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize());
        Assertions.assertEquals(0, program.getActorDefinitionList().getActorDefinition("Stage").get().getScripts().getSize());
        LlmResponseParser responseParser = new LlmResponseParser();
        var parsedResponse = responseParser.parseLLMResponse(response);
        Program updatedProgram = responseParser.updateProgram(program, parsedResponse);
        Assertions.assertEquals(1, updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize());
        List<Script> scripts = updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getScriptList();
        Script newScript = scripts.get(0);
        Assertions.assertEquals(1, newScript.getStmtList().getStmts().size());
        Assertions.assertInstanceOf(Say.class, newScript.getStmtList().getStmts().get(0));

        Assertions.assertEquals(1, updatedProgram.getActorDefinitionList().getActorDefinition("Stage").get().getScripts().getSize());
        scripts = updatedProgram.getActorDefinitionList().getActorDefinition("Stage").get().getScripts().getScriptList();
        newScript = scripts.get(0);
        Assertions.assertEquals(1, newScript.getStmtList().getStmts().size());
        Assertions.assertInstanceOf(StageClicked.class, newScript.getEvent());
        Assertions.assertInstanceOf(Think.class, newScript.getStmtList().getStmts().get(0));
        Think think = (Think) newScript.getStmtList().getStmts().get(0);
        Assertions.assertInstanceOf(AsString.class, think.getThought());
        AsString asString = (AsString) think.getThought();
        Assertions.assertInstanceOf(Qualified.class, asString.getOperand1());
    }

    @Test
    void testChangeAndAddNewScript() throws ParsingException, IOException {
        String response = """
                scratch
                //Sprite: Sprite1
                //Script: V/6:G4i[HL#.bvM4XA|8
                when green flag clicked
                broadcast (test v)
                forever
                    if <key (space v) pressed?> then
                        turn right (15) degrees
                    end
                end

                //Script: newlyadded
                when green flag clicked
                say [test]
                """;
        Program program = getAST("./src/test/fixtures/playerSpriteMissingLoop.json");
        Assertions.assertEquals(1, program.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize());
        Assertions.assertEquals(0, program.getSymbolTable().getMessages().size());
        LlmResponseParser responseParser = new LlmResponseParser();
        var parsedResponse = responseParser.parseLLMResponse(response);
        Program updatedProgram = responseParser.updateProgram(program, parsedResponse);
        Assertions.assertEquals(2, updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize());
        List<Script> scripts = updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getScriptList();
        Script newScript = scripts.get(1);
        Assertions.assertEquals(1, newScript.getStmtList().getStmts().size());
        Assertions.assertInstanceOf(Say.class, newScript.getStmtList().getStmts().get(0));

        Assertions.assertEquals(1, updatedProgram.getSymbolTable().getMessages().size());
        Assertions.assertTrue(updatedProgram.getSymbolTable().getMessage("test").isPresent());
        Script modified = scripts.get(0);
        Assertions.assertEquals(2, modified.getStmtList().getStmts().size());
        Assertions.assertInstanceOf(Broadcast.class, modified.getStmtList().getStmts().get(0));
    }

    @Test
    void testAddProcedureAndCall() throws ParsingException, IOException {
        String response = """
                scratch
                //Sprite: Sprite1
                //Script: newProcedure
                define test (message)
                broadcast (message)
                forever
                    if <key (space v) pressed?> then
                        turn right (15) degrees
                    end
                end

                //Script: newlyadded
                when green flag clicked
                test (newMessage)
                """;
        Program program = getAST("./src/test/fixtures/playerSpriteMissingLoop.json");
        Assertions.assertEquals(1, program.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize());
        Assertions.assertEquals(0, program.getActorDefinitionList().getActorDefinition("Sprite1").get().getProcedureDefinitionList().getList().size());
        Assertions.assertEquals(0, program.getSymbolTable().getMessages().size());
        LlmResponseParser responseParser = new LlmResponseParser();
        var parsedResponse = responseParser.parseLLMResponse(response);
        Program updatedProgram = responseParser.updateProgram(program, parsedResponse);
        Assertions.assertEquals(2, updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize());
        List<Script> scripts = updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getScriptList();
        Script newScript = scripts.get(1);
        Assertions.assertEquals(1, newScript.getStmtList().getStmts().size());
        Assertions.assertInstanceOf(CallStmt.class, newScript.getStmtList().getStmts().get(0));

        Assertions.assertEquals(0, updatedProgram.getSymbolTable().getMessages().size());
        Assertions.assertEquals(1, updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getProcedureDefinitionList().getList().size());
        ProcedureDefinitionList procedureDefinitionList = updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getProcedureDefinitionList();
        ProcedureDefinition modified = procedureDefinitionList.getList().get(0);
        Assertions.assertEquals(2, modified.getStmtList().getStmts().size());
        Assertions.assertInstanceOf(Broadcast.class, modified.getStmtList().getStmts().get(0));
    }


    @Test
    void testReplaceProcedure() throws ParsingException, IOException {
        String response = """
                scratch
                //Sprite: Sprite1
                //Script: M1JuO/3cOhiW+SGck?Vd
                define testBlock
                broadcast (message)
                forever
                    if <key (space v) pressed?> then
                        turn right (15) degrees
                    end
                end
                """;
        Program program = getAST("./src/test/fixtures/singleProcedure.json");
        Assertions.assertEquals(0, program.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize());
        Assertions.assertEquals(1, program.getActorDefinitionList().getActorDefinition("Sprite1").get().getProcedureDefinitionList().getList().size());
        Assertions.assertEquals(0, program.getSymbolTable().getMessages().size());
        LlmResponseParser responseParser = new LlmResponseParser();
        var parsedResponse = responseParser.parseLLMResponse(response);
        Program updatedProgram = responseParser.updateProgram(program, parsedResponse);
        Assertions.assertEquals(0, updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize());

        Assertions.assertEquals(0, updatedProgram.getSymbolTable().getMessages().size());
        Assertions.assertEquals(1, updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getProcedureDefinitionList().getList().size());
        ProcedureDefinitionList procedureDefinitionList = updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getProcedureDefinitionList();
        ProcedureDefinition modified = procedureDefinitionList.getList().get(0);
        Assertions.assertEquals(2, modified.getStmtList().getStmts().size());
        Assertions.assertInstanceOf(Broadcast.class, modified.getStmtList().getStmts().get(0));
    }
}
