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
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.MissingLoopSensing;
import de.uni_passau.fim.se2.litterbox.analytics.llm.LLMResponseParser;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;
import de.uni_passau.fim.se2.litterbox.jsoncreation.JSONStringCreator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

public class ScratchBlocksToJSONTest implements JsonTest {

    @Test
    void testParseJSONParsedFromScratchBlocks() throws ParsingException, IOException {

        String response = """
                scratch
                //Sprite: Sprite1
                //Script: V/6:G4i[HL#.bvM4XA|8
                when green flag clicked
                set rotation to (0)
                forever
                    if <key (space v) pressed?> then
                        turn right (15) degrees
                    end
                end
                """;
        Program program = getAST("./src/test/fixtures/playerSpriteMissingLoop.json");
        LLMResponseParser responseParser = new LLMResponseParser();
        Program updatedProgram = responseParser.parseResultAndUpdateProgram(program, response);
        String updatedJson = JSONStringCreator.createProgramJSONString(updatedProgram);

        Scratch3Parser parser = new Scratch3Parser();
        Program parsedProgram = parser.parseString("example", updatedJson);

        assertThat(parsedProgram.getActorDefinitionList().getDefinitions()).hasSize(2);
        assertThat(parsedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize()).isEqualTo(1);
        Set<Issue> modifiedIssues = new MissingLoopSensing().check(parsedProgram);
        assertThat(modifiedIssues).isEmpty();
    }
}
