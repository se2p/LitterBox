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
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.MissingLoopSensing;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.llm.ScratchLLM;
import de.uni_passau.fim.se2.litterbox.llm.api.OpenAiApi;
import de.uni_passau.fim.se2.litterbox.llm.prompts.DefaultPrompts;
import de.uni_passau.fim.se2.litterbox.llm.prompts.QueryTarget;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LLMProgramImprovementAnalyzerTest implements JsonTest {

    @Test
    void testFixBugInTargetScript() throws ParsingException, IOException {

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
        Script script = program.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getScript(0);
        String scriptID = AstNodeUtil.getBlockId(script.getEvent());
        QueryTarget target = new QueryTarget.ScriptTarget(scriptID);

        ScratchLLM<OpenAiApi, DefaultPrompts> llm = mock(ScratchLLM.class);
        when(llm.improve(any(), any(), anyString(), anyBoolean())).thenReturn(response);

        LLMProgramImprovementAnalyzer analyzer = new LLMProgramImprovementAnalyzer(target, "missing_loop_sensing", true, llm);

        assertThat(program.getActorDefinitionList().getDefinitions()).hasSize(2);
        assertThat(program.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize()).isEqualTo(1);
        MissingLoopSensing loopSensing = new MissingLoopSensing();
        Set<Issue> originalIssues = loopSensing.check(program);
        assertThat(originalIssues).hasSize(1);

        Program modifiedProgram = analyzer.analyze(program);
        assertThat(modifiedProgram.getActorDefinitionList().getDefinitions()).hasSize(2);
        assertThat(modifiedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize()).isEqualTo(1);
        Set<Issue> modifiedIssues = loopSensing.check(modifiedProgram);
        assertThat(modifiedIssues).isEmpty();
    }

    @Test
    void testFixBugInTargetScriptWithMultipleScripts() throws ParsingException, IOException {

        String response = """
                scratch
                //Sprite: Sprite1

                //Script: V/6:G4i[HL#.bvM4XA|8
                when green flag clicked
                forever
                  if <key (space v) pressed?> then
                    turn right (15) degrees
                  end
                end

                //Script: ^zj@X}R4N`_I{(7*vN63
                when green flag clicked
                if <key (up arrow v) pressed?> then
                turn left (15) degrees
                end
                """;
        Program program = getAST("./src/test/fixtures/playerSpriteMissingLoopTwice.json");

        Script script = program.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getScript(0);
        String scriptID = AstNodeUtil.getBlockId(script.getEvent());
        QueryTarget target = new QueryTarget.ScriptTarget(scriptID);

        ScratchLLM<OpenAiApi, DefaultPrompts> llm = mock(ScratchLLM.class);
        when(llm.improve(any(), any(), anyString(), anyBoolean())).thenReturn(response);

        LLMProgramImprovementAnalyzer analyzer = new LLMProgramImprovementAnalyzer(target, "missing_loop_sensing", true, llm);

        assertThat(program.getActorDefinitionList().getDefinitions()).hasSize(2);
        assertThat(program.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize()).isEqualTo(2);
        MissingLoopSensing loopSensing = new MissingLoopSensing();
        Set<Issue> originalIssues = loopSensing.check(program);
        assertThat(originalIssues).hasSize(2);

        Program modifiedProgram = analyzer.analyze(program);
        assertThat(modifiedProgram.getActorDefinitionList().getDefinitions()).hasSize(2);
        assertThat(modifiedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize()).isEqualTo(2);
        Set<Issue> modifiedIssues = loopSensing.check(modifiedProgram);
        assertThat(modifiedIssues).hasSize(1);
    }


    @Test
    void testFixBugInSpriteWithMultipleScripts() throws ParsingException, IOException {

        String response = """
                scratch
                //Sprite: Sprite1

                //Script: V/6:G4i[HL#.bvM4XA|8
                when green flag clicked
                forever
                  if <key (space v) pressed?> then
                    turn right (15) degrees
                  end
                end

                //Script: ^zj@X}R4N`_I{(7*vN63
                when green flag clicked
                forever
                  if <key (up arrow v) pressed?> then
                    turn left (15) degrees
                  end
                end
                """;
        Program program = getAST("./src/test/fixtures/playerSpriteMissingLoopTwice.json");

        QueryTarget target = new QueryTarget.SpriteTarget("Sprite1");

        ScratchLLM<OpenAiApi, DefaultPrompts> llm = mock(ScratchLLM.class);
        when(llm.improve(any(), any(), anyString(), anyBoolean())).thenReturn(response);

        LLMProgramImprovementAnalyzer analyzer = new LLMProgramImprovementAnalyzer(target, "missing_loop_sensing", true, llm);

        assertThat(program.getActorDefinitionList().getDefinitions()).hasSize(2);
        assertThat(program.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize()).isEqualTo(2);
        MissingLoopSensing loopSensing = new MissingLoopSensing();
        Set<Issue> originalIssues = loopSensing.check(program);
        assertThat(originalIssues).hasSize(2);

        Program modifiedProgram = analyzer.analyze(program);
        assertThat(modifiedProgram.getActorDefinitionList().getDefinitions()).hasSize(2);
        assertThat(modifiedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize()).isEqualTo(2);
        Set<Issue> modifiedIssues = loopSensing.check(modifiedProgram);
        assertThat(modifiedIssues).isEmpty();
    }

    @Test
    void testFixBugInProgram() throws ParsingException, IOException {

        String response = """
                scratch
                //Sprite: Sprite1

                //Script: V/6:G4i[HL#.bvM4XA|8
                when green flag clicked
                forever
                  if <key (space v) pressed?> then
                    turn right (15) degrees
                  end
                end

                //Script: ^zj@X}R4N`_I{(7*vN63
                when green flag clicked
                forever
                  if <key (up arrow v) pressed?> then
                    turn left (15) degrees
                  end
                end
                """;
        Program program = getAST("./src/test/fixtures/playerSpriteMissingLoopTwice.json");

        QueryTarget target = new QueryTarget.ProgramTarget();

        ScratchLLM<OpenAiApi, DefaultPrompts> llm = mock(ScratchLLM.class);
        when(llm.improve(any(), any(), anyString(), anyBoolean())).thenReturn(response);

        LLMProgramImprovementAnalyzer analyzer = new LLMProgramImprovementAnalyzer(target, "missing_loop_sensing", true, llm);

        assertThat(program.getActorDefinitionList().getDefinitions()).hasSize(2);
        assertThat(program.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize()).isEqualTo(2);
        MissingLoopSensing loopSensing = new MissingLoopSensing();
        Set<Issue> originalIssues = loopSensing.check(program);
        assertThat(originalIssues).hasSize(2);

        Program modifiedProgram = analyzer.analyze(program);
        assertThat(modifiedProgram.getActorDefinitionList().getDefinitions()).hasSize(2);
        assertThat(modifiedProgram.getActorDefinitionList().getActorDefinition("Sprite1").get().getScripts().getSize()).isEqualTo(2);
        Set<Issue> modifiedIssues = loopSensing.check(modifiedProgram);
        assertThat(modifiedIssues).isEmpty();
    }

}
