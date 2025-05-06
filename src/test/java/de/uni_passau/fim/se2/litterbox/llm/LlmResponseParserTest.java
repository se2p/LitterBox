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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

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
}
