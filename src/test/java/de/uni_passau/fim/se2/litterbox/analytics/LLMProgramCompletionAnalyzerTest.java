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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.metric.BlockCount;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.llm.ScratchLLM;
import de.uni_passau.fim.se2.litterbox.llm.prompts.QueryTarget;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LLMProgramCompletionAnalyzerTest implements JsonTest {

    private int countBlocks(Script script) {
        BlockCount<Script> countBlocks = new BlockCount<>();
        return (int) countBlocks.calculateMetric(script);
    }

    @Test
    void testExtendScript() throws ParsingException, IOException {

        String response = """
                scratch
                //Sprite: Sprite1
                //Script: V/6:G4i[HL#.bvM4XA|8
                when green flag clicked
                if <key (space v) pressed?> then
                    turn right (15) degrees
                end
                set rotation to (0)
                set rotation to (0)
                set rotation to (0)
                """;
        Program program = getAST("./src/test/fixtures/playerSpriteMissingLoop.json");
        Script script = program.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getScript(0);
        String scriptID = AstNodeUtil.getBlockId(script.getEvent());

        QueryTarget target = new QueryTarget.ScriptTarget(scriptID);

        ScratchLLM llm = mock(ScratchLLM.class);
        when(llm.autoComplete(any(), any())).thenReturn(response);

        LLMProgramCompletionAnalyzer analyzer = new LLMProgramCompletionAnalyzer(target, true, llm);

        assertThat(program.getActorDefinitionList().getDefinitions()).hasSize(2);
        assertThat(program.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize()).isEqualTo(1);
        assertThat(countBlocks(program.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getScript(0))).isEqualTo(5);

        Program modifiedProgram = analyzer.analyze(program);
        assertThat(modifiedProgram.getActorDefinitionList().getDefinitions()).hasSize(2);
        assertThat(modifiedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize()).isEqualTo(1);
        assertThat(countBlocks(modifiedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getScript(0))).isEqualTo(8);
    }


    @Test
    void testExtendScriptInSprite() throws ParsingException, IOException {

        String response = """
                scratch
                //Sprite: Sprite1
                //Script: V/6:G4i[HL#.bvM4XA|8
                when green flag clicked
                if <key (space v) pressed?> then
                    turn right (15) degrees
                end
                set rotation to (0)
                set rotation to (0)
                set rotation to (0)
                """;
        Program program = getAST("./src/test/fixtures/playerSpriteMissingLoop.json");
        QueryTarget target = new QueryTarget.SpriteTarget("Sprite1");

        ScratchLLM llm = mock(ScratchLLM.class);
        when(llm.autoComplete(any(), any())).thenReturn(response);

        LLMProgramCompletionAnalyzer analyzer = new LLMProgramCompletionAnalyzer(target, true, llm);

        assertThat(program.getActorDefinitionList().getDefinitions()).hasSize(2);
        assertThat(program.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize()).isEqualTo(1);
        assertThat(countBlocks(program.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getScript(0))).isEqualTo(5);

        Program modifiedProgram = analyzer.analyze(program);
        assertThat(modifiedProgram.getActorDefinitionList().getDefinitions()).hasSize(2);
        assertThat(modifiedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize()).isEqualTo(1);
        assertThat(countBlocks(modifiedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getScript(0))).isEqualTo(8);
    }

    @Test
    void testExtendSpriteWithNewScript() throws ParsingException, IOException {

        String response = """
                scratch
                //Sprite: Sprite1
                //Script: V/6:G4i[HL#.bvM4XA|8
                when green flag clicked
                if <key (space v) pressed?> then
                turn right (15) degrees
                end
                //Script: V/6:G4i[HL#.bvM4XA|9
                when green flag clicked
                forever
                if <key (up arrow v) pressed?> then
                move (10) steps
                end
                end
                """;
        Program program = getAST("./src/test/fixtures/playerSpriteMissingLoop.json");
        QueryTarget target = new QueryTarget.SpriteTarget("Sprite1");

        ScratchLLM llm = mock(ScratchLLM.class);
        when(llm.autoComplete(any(), any())).thenReturn(response);

        LLMProgramCompletionAnalyzer analyzer = new LLMProgramCompletionAnalyzer(target, true, llm);

        assertThat(program.getActorDefinitionList().getDefinitions()).hasSize(2);
        assertThat(program.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize()).isEqualTo(1);
        assertThat(countBlocks(program.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getScript(0))).isEqualTo(5);

        Program modifiedProgram = analyzer.analyze(program);
        assertThat(modifiedProgram.getActorDefinitionList().getDefinitions()).hasSize(2);
        assertThat(modifiedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize()).isEqualTo(2);
        assertThat(countBlocks(program.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getScript(0))).isEqualTo(5);
        assertThat(countBlocks(modifiedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getScript(1))).isEqualTo(6);
    }

    @Test
    void testExtendProgram() throws ParsingException, IOException {

        String response = """
                scratch
                //Sprite: Sprite1
                //Script: V/6:G4i[HL#.bvM4XA|8
                when green flag clicked
                if <key (space v) pressed?> then
                turn right (15) degrees
                end
                //Script: V/6:G4i[HL#.bvM4XA|9
                when green flag clicked
                forever
                if <key (up arrow v) pressed?> then
                move (10) steps
                end
                end
                """;
        Program program = getAST("./src/test/fixtures/playerSpriteMissingLoop.json");
        QueryTarget target = new QueryTarget.ProgramTarget();

        ScratchLLM llm = mock(ScratchLLM.class);
        when(llm.autoComplete(any(), any())).thenReturn(response);

        LLMProgramCompletionAnalyzer analyzer = new LLMProgramCompletionAnalyzer(target, true, llm);

        assertThat(program.getActorDefinitionList().getDefinitions()).hasSize(2);
        assertThat(program.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize()).isEqualTo(1);
        assertThat(countBlocks(program.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getScript(0))).isEqualTo(5);

        Program modifiedProgram = analyzer.analyze(program);
        assertThat(modifiedProgram.getActorDefinitionList().getDefinitions()).hasSize(2);
        assertThat(modifiedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize()).isEqualTo(2);
        assertThat(countBlocks(program.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getScript(0))).isEqualTo(5);
        assertThat(countBlocks(modifiedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getScript(1))).isEqualTo(6);
    }
}
