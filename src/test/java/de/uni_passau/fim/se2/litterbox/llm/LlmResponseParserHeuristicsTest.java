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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ChangeGraphicEffectBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.TurnRight;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class LlmResponseParserHeuristicsTest implements JsonTest {

    @Test
    void testTurnCwHeuristic() throws ParsingException, IOException {
        String response = """
                scratch
                //Sprite: Sprite1
                //Script: newlyadded
                when green flag clicked
                turn cw <15> degrees
                """;
        Program program = getAST("./src/test/fixtures/emptyProject.json");
        LlmResponseParser responseParser = new LlmResponseParser();
        var parsedResponse = responseParser.parseLLMResponse(response);
        Program updatedProgram = responseParser.updateProgram(program, parsedResponse);
        
        List<Script> scripts = updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getScriptList();
        Script newScript = scripts.getFirst();
        assertEquals(1, newScript.getStmtList().getStmts().size());
        assertInstanceOf(TurnRight.class, newScript.getStmtList().getStmts().getFirst());
        TurnRight turnRight = (TurnRight) newScript.getStmtList().getStmts().getFirst();
        assertInstanceOf(NumberLiteral.class, turnRight.getDegrees());
        assertEquals(15.0, ((NumberLiteral) turnRight.getDegrees()).getValue());
    }

    @Test
    void testChangeColorEffectHeuristic() throws ParsingException, IOException {
        String response = """
                scratch
                //Sprite: Sprite1
                //Script: newlyadded
                when green flag clicked
                change color effect by (20)
                """;
        Program program = getAST("./src/test/fixtures/emptyProject.json");
        LlmResponseParser responseParser = new LlmResponseParser();
        var parsedResponse = responseParser.parseLLMResponse(response);
        Program updatedProgram = responseParser.updateProgram(program, parsedResponse);
        
        List<Script> scripts = updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getScriptList();
        Script newScript = scripts.getFirst();
        assertEquals(1, newScript.getStmtList().getStmts().size());
        assertInstanceOf(ChangeGraphicEffectBy.class, newScript.getStmtList().getStmts().getFirst());
        ChangeGraphicEffectBy changeEffect = (ChangeGraphicEffectBy) newScript.getStmtList().getStmts().getFirst();
        assertInstanceOf(NumberLiteral.class, changeEffect.getValue());
        assertEquals(20.0, ((NumberLiteral) changeEffect.getValue()).getValue());
    }

    @Test
    void testMoveStepsHeuristic() throws ParsingException, IOException {
        String response = """
                scratch
                //Sprite: Sprite1
                //Script: newlyadded
                when green flag clicked
                move <10> steps
                """;
        Program program = getAST("./src/test/fixtures/emptyProject.json");
        LlmResponseParser responseParser = new LlmResponseParser();
        var parsedResponse = responseParser.parseLLMResponse(response);
        Program updatedProgram = responseParser.updateProgram(program, parsedResponse);
        
        List<Script> scripts = updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getScriptList();
        Script newScript = scripts.getFirst();
        assertEquals(1, newScript.getStmtList().getStmts().size());
        assertInstanceOf(MoveSteps.class, newScript.getStmtList().getStmts().getFirst());
        MoveSteps moveSteps = (MoveSteps) newScript.getStmtList().getStmts().getFirst();
        assertInstanceOf(NumberLiteral.class, moveSteps.getSteps());
        assertEquals(10.0, ((NumberLiteral) moveSteps.getSteps()).getValue());
    }

    @Test
    void testGeneralNumberHeuristic() throws ParsingException, IOException {
        String response = """
                scratch
                //Sprite: Sprite1
                //Script: newlyadded
                when green flag clicked
                wait <0.5> seconds
                """;
        Program program = getAST("./src/test/fixtures/emptyProject.json");
        LlmResponseParser responseParser = new LlmResponseParser();
        var parsedResponse = responseParser.parseLLMResponse(response);
        Program updatedProgram = responseParser.updateProgram(program, parsedResponse);
        
        List<Script> scripts = updatedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getScriptList();
        Script newScript = scripts.getFirst();
        assertEquals(1, newScript.getStmtList().getStmts().size());
        assertInstanceOf(WaitSeconds.class, newScript.getStmtList().getStmts().getFirst());
        WaitSeconds wait = (WaitSeconds) newScript.getStmtList().getStmts().getFirst();
        assertInstanceOf(NumberLiteral.class, wait.getSeconds());
        assertEquals(0.5, ((NumberLiteral) wait.getSeconds()).getValue());
    }
}
