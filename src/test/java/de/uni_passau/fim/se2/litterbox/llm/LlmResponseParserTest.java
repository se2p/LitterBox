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
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StageClicked;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.DistanceTo;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Username;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Think;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.PointTowards;
import de.uni_passau.fim.se2.litterbox.ast.visitor.BlockByIdFinder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        assertEquals(1, program.getSymbolTable().getVariables().size());
        assertEquals(9, program.getActorDefinitionList().getDefinitions().get(1).getSetStmtList().getStmts().size());
        LlmResponseParser responseParser = new LlmResponseParser();
        var parsedResponse = responseParser.parseLLMResponse(response);
        Program updatedProgram = responseParser.updateProgram(program, parsedResponse);
        assertEquals(2, updatedProgram.getSymbolTable().getVariables().size());
        assertEquals(10, updatedProgram.getActorDefinitionList().getDefinitions().get(1).getSetStmtList().getStmts().size());
    }

    @Test
    void testReplaceExistingScript() throws ParsingException, IOException {
        String response = """
                scratch
                //Sprite: Sprite1
                //Script: V/6:G4i[HL#.bvM4XA|8 (ignored suffix)
                when green flag clicked
                forever
                    if <key (space v) pressed?> then
                        turn right (15) degrees
                    end
                end
                """;
        Program program = getAST("./src/test/fixtures/playerSpriteMissingLoop.json");
        assertThat(program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList())
                .hasSize(1);

        LlmResponseParser responseParser = new LlmResponseParser();
        var parsedResponse = responseParser.parseLLMResponse(response);
        Program updatedProgram = responseParser.updateProgram(program, parsedResponse);

        assertThat(updatedProgram.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList())
                .hasSize(1);
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
        assertEquals(0, program.getSymbolTable().getMessages().size());
        LlmResponseParser responseParser = new LlmResponseParser();
        var parsedResponse = responseParser.parseLLMResponse(response);
        Program updatedProgram = responseParser.updateProgram(program, parsedResponse);
        assertEquals(1, updatedProgram.getSymbolTable().getMessages().size());
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
        assertEquals(1, program.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize());
        LlmResponseParser responseParser = new LlmResponseParser();
        var parsedResponse = responseParser.parseLLMResponse(response);
        Program updatedProgram = responseParser.updateProgram(program, parsedResponse);
        assertEquals(2, updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize());
        List<Script> scripts = updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getScriptList();
        Script newScript = scripts.get(1);
        assertEquals(1, newScript.getStmtList().getStmts().size());
        assertInstanceOf(Say.class, newScript.getStmtList().getStmts().get(0));
    }

    @Test
    void testAddNewBrokenScript() throws ParsingException, IOException {
        String response = """
                scratch
                //Sprite: Sprite1
                //Script: newlyadded
                when green flag clicked
                forever {
                wait (0.1) secs
                }
                if <touching color [#ffffff] ?> {
                    move (-1) steps
                }
                """;
        Program program = getAST("./src/test/fixtures/playerSpriteMissingLoop.json");
        assertEquals(1, program.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize());

        LlmResponseParser responseParser = new LlmResponseParser();
        var parsedResponse = responseParser.parseLLMResponse(response);
        Program updatedProgram = responseParser.updateProgram(program, parsedResponse);

        assertEquals(2, updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize());

        List<Script> scripts = updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getScriptList();
        Script newScript = scripts.get(1);

        assertEquals(2, newScript.getStmtList().getStmts().size());
        assertInstanceOf(RepeatForeverStmt.class, newScript.getStmtList().getStmts().get(0));
        assertInstanceOf(IfThenStmt.class, newScript.getStmtList().getStmts().get(1));
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
        assertEquals(0, program.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize());
        assertEquals(0, program.getActorDefinitionList().getActorDefinition("Stage").get().getScripts().getSize());
        LlmResponseParser responseParser = new LlmResponseParser();
        var parsedResponse = responseParser.parseLLMResponse(response);
        Program updatedProgram = responseParser.updateProgram(program, parsedResponse);
        assertEquals(1, updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize());
        List<Script> scripts = updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getScriptList();
        Script newScript = scripts.get(0);
        assertEquals(1, newScript.getStmtList().getStmts().size());
        assertInstanceOf(Say.class, newScript.getStmtList().getStmts().get(0));

        assertEquals(1, updatedProgram.getActorDefinitionList().getActorDefinition("Stage").get().getScripts().getSize());
        scripts = updatedProgram.getActorDefinitionList().getActorDefinition("Stage").get().getScripts().getScriptList();
        newScript = scripts.get(0);
        assertEquals(1, newScript.getStmtList().getStmts().size());
        assertInstanceOf(StageClicked.class, newScript.getEvent());
        assertInstanceOf(Think.class, newScript.getStmtList().getStmts().get(0));
        Think think = (Think) newScript.getStmtList().getStmts().get(0);
        assertInstanceOf(AsString.class, think.getThought());
        AsString asString = (AsString) think.getThought();
        assertInstanceOf(Qualified.class, asString.getOperand1());
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
        assertEquals(1, program.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize());
        assertEquals(0, program.getSymbolTable().getMessages().size());
        LlmResponseParser responseParser = new LlmResponseParser();
        var parsedResponse = responseParser.parseLLMResponse(response);
        Program updatedProgram = responseParser.updateProgram(program, parsedResponse);
        assertEquals(2, updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize());
        List<Script> scripts = updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getScriptList();
        Script newScript = scripts.get(1);
        assertEquals(1, newScript.getStmtList().getStmts().size());
        assertInstanceOf(Say.class, newScript.getStmtList().getStmts().get(0));

        assertEquals(1, updatedProgram.getSymbolTable().getMessages().size());
        Assertions.assertTrue(updatedProgram.getSymbolTable().getMessage("test").isPresent());
        Script modified = scripts.get(0);
        assertEquals(2, modified.getStmtList().getStmts().size());
        assertInstanceOf(Broadcast.class, modified.getStmtList().getStmts().get(0));
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
        assertEquals(1, program.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize());
        assertEquals(0, program.getActorDefinitionList().getActorDefinition("Sprite1").get().getProcedureDefinitionList().getList().size());
        assertEquals(0, program.getSymbolTable().getMessages().size());
        LlmResponseParser responseParser = new LlmResponseParser();
        var parsedResponse = responseParser.parseLLMResponse(response);
        Program updatedProgram = responseParser.updateProgram(program, parsedResponse);
        assertEquals(2, updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize());
        List<Script> scripts = updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getScriptList();
        Script newScript = scripts.get(1);
        assertEquals(1, newScript.getStmtList().getStmts().size());
        assertInstanceOf(CallStmt.class, newScript.getStmtList().getStmts().get(0));

        assertEquals(0, updatedProgram.getSymbolTable().getMessages().size());
        assertEquals(1, updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getProcedureDefinitionList().getList().size());
        ProcedureDefinitionList procedureDefinitionList = updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getProcedureDefinitionList();
        ProcedureDefinition modified = procedureDefinitionList.getList().get(0);
        assertEquals(2, modified.getStmtList().getStmts().size());
        assertInstanceOf(Broadcast.class, modified.getStmtList().getStmts().get(0));
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
        assertEquals(0, program.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize());
        assertEquals(1, program.getActorDefinitionList().getActorDefinition("Sprite1").get().getProcedureDefinitionList().getList().size());
        assertEquals(0, program.getSymbolTable().getMessages().size());
        LlmResponseParser responseParser = new LlmResponseParser();
        var parsedResponse = responseParser.parseLLMResponse(response);
        Program updatedProgram = responseParser.updateProgram(program, parsedResponse);
        assertEquals(0, updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize());

        assertEquals(0, updatedProgram.getSymbolTable().getMessages().size());
        assertEquals(1, updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getProcedureDefinitionList().getList().size());
        ProcedureDefinitionList procedureDefinitionList = updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getProcedureDefinitionList();
        ProcedureDefinition modified = procedureDefinitionList.getList().get(0);
        assertEquals(2, modified.getStmtList().getStmts().size());
        assertInstanceOf(Broadcast.class, modified.getStmtList().getStmts().get(0));
    }

    @Test
    void testParseSingleScriptWithBrokenComments() throws ParsingException, IOException {
        final String response = """
                ```scratchblocks
                sprite:Sprite1
                Script: Z`w:F,*y_,WJPBw^$]b)
                when green flag clicked
                ask (What is your name?) and wait
                say (answer) for (2) seconds
                ```
                """;
        Program program = getAST("./src/test/fixtures/bugpattern/missingAsk.json");

        final LlmResponseParser parser = new LlmResponseParser();
        final Script script = (Script) BlockByIdFinder.findBlock(program, "Z`w:F,*y_,WJPBw^$]b)")
                .orElseThrow()
                .getParentNode();

        final ParsedLlmResponseCode parsedResponse = parser.parseLLMResponse(response);
        final ScriptEntity parsedScript = parser.extractUpdatedScriptFromResponse(script, parsedResponse);
        assertNotNull(parsedScript);
    }

    @Test
    void testParsingBrokenActor() throws ParsingException, IOException {
        String response = """
                when green flag clicked
                set size to (70) %
                go to x: (-140) y: (-68)
                repeat (50)
                change size by (-1)
                wait (0.07) seconds
                end

                //Script: %jW.on0S!7T_N~uGzae%
                when green flag clicked
                say [Ich möchte dir eine Geschichte erzählen: Wir sind über das Meer zur Schatzinsel gefahren.]
                glide (pick random (4) to (9)) secs to x: (110) y: (-70)
                say [Wir sind vielen Gefahren begegnet.]
                glide (3) secs to x: (90) y: (20)
                say [Es gab viele aktive Vulkane auf der Insel.]
                glide (2) secs to x: (10) y: (10)
                glide (2) secs to x: (-20) y: (70)
                say [Endlich hatten wir den Schatz erreicht. ] for (3) seconds
                say [Doch den größten Schatz habe ich auf dem Heimweg kennengelernt.] for (3) seconds
                say [Was für ein Happy End!] for (2) seconds
                broadcast [Was für ein Happy End! v]

                //Script: (N($V}4*2nGPPfl;|)3U
                when I receive [Was für ein Happy End! v]
                repeat (25)
                change y by (-1)
                change x by (-1)
                change size by (3)
                change y by (-1)
                change x by (-1)
                wait (0.1) seconds
                end

                //Sprite: Schatzkarte
                //Script: {Ib.y`:[%-GbceURJSz8
                when green flag clicked
                go to x: (0) y: (-20)
                go to [back v] layer

                //Sprite: Vulkaninsel
                //Script: y/ia3*ysX:*;A=0*R1*(
                when green flag clicked
                go to x: (-58) y: (10)
                hide

                //Script: YT$[^Byo/lJuc1G3U8Hv
                when green flag clicked
                wait until <(distance to (Pirat v)) < (70)>
                show

                //Sprite: Schatztruhe
                //Script: LM~~l[OmZl0bmz)EEM7]
                when green flag clicked
                hide
                go to x: (-80) y: (80)

                //Script: k:yU9Y%G53w%z7U7fimM
                when green flag clicked
                wait until <(distance to (Pirat v)) < (65)>
                show

                //Sprite: Mädchen
                //Script: 15j}eA1Kjp;6rE18sX1r
                when green flag clicked
                hide
                set size to (20) %
                go to x: (100) y: (-20)

                //Script: $SI%l3E!sP=|]4#fF][o
                when I receive [Was für ein Happy End! v]
                show
                repeat (25)
                change size by (3)
                wait (0.1) seconds
                end
                """;
        Program program = getAST("./src/test/fixtures/Treasure.json");
        LlmResponseParser responseParser = new LlmResponseParser();
        var parsedResponse = responseParser.parseLLMResponse(response);
        Program updatedProgram = responseParser.updateProgram(program, parsedResponse);
        assertEquals(7, updatedProgram.getActorDefinitionList().getDefinitions().size());
        Assertions.assertTrue(updatedProgram.getActorDefinitionList().getActorDefinition("unidentifiedActor").isPresent());
    }

    @Test
    void testAddDistance() throws ParsingException, IOException {
        String response = """
                scratch
                //Sprite: Sprite1
                //Script: newlyadded
                when green flag clicked
                move (distance to (other v)) steps
                move (distance to (mouse-pointer v)) steps
                move (distance to (username)) steps
                point towards (other v)
                """;
        Program program = getAST("./src/test/fixtures/emptyProject.json");
        LlmResponseParser responseParser = new LlmResponseParser();
        var parsedResponse = responseParser.parseLLMResponse(response);
        Program updatedProgram = responseParser.updateProgram(program, parsedResponse);
        assertEquals(1, updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize());
        List<Script> scripts = updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getScriptList();
        Script newScript = scripts.get(0);
        assertEquals(4, newScript.getStmtList().getStmts().size());
        for (int i = 0; i < 3; i++) {
            assertInstanceOf(MoveSteps.class, newScript.getStmtList().getStmts().get(i));
        }
        assertInstanceOf(PointTowards.class, newScript.getStmtList().getStmts().get(3));

        MoveSteps move = (MoveSteps) newScript.getStmtList().getStmts().get(0);
        assertInstanceOf(DistanceTo.class, move.getSteps());
        DistanceTo distance = (DistanceTo) move.getSteps();
        assertInstanceOf(FromExpression.class, distance.getPosition());
        FromExpression from = (FromExpression) distance.getPosition();
        assertInstanceOf(AsString.class, from.getStringExpr());

        move = (MoveSteps) newScript.getStmtList().getStmts().get(1);
        assertInstanceOf(DistanceTo.class, move.getSteps());
        distance = (DistanceTo) move.getSteps();
        assertInstanceOf(MousePos.class, distance.getPosition());

        move = (MoveSteps) newScript.getStmtList().getStmts().get(2);
        assertInstanceOf(DistanceTo.class, move.getSteps());
        distance = (DistanceTo) move.getSteps();
        assertInstanceOf(FromExpression.class, distance.getPosition());
        from = (FromExpression) distance.getPosition();
        assertInstanceOf(Username.class, from.getStringExpr());

        PointTowards pointTowards = (PointTowards) newScript.getStmtList().getStmts().get(3);
        assertInstanceOf(FromExpression.class, pointTowards.getPosition());
        from = (FromExpression) pointTowards.getPosition();
        assertInstanceOf(AsString.class, from.getStringExpr());
    }

    @Test
    void testAddActor() throws ParsingException, IOException {
        String response = """
                scratch
                //Sprite: CatNew
                //Script: newlyadded
                when green flag clicked
                move (distance to (Sprite1 v)) steps
                """;
        Program program = getAST("./src/test/fixtures/emptyProject.json");
        LlmResponseParser responseParser = new LlmResponseParser();
        var parsedResponse = responseParser.parseLLMResponse(response);
        Program updatedProgram = responseParser.updateProgram(program, parsedResponse);
        assertEquals(3, updatedProgram.getActorDefinitionList().getDefinitions().size());
        ActorDefinition newActor = updatedProgram.getActorDefinitionList().getActorDefinition("CatNew").get();
        Assertions.assertFalse(newActor.getSetStmtList().getStmts().isEmpty());
    }
}
