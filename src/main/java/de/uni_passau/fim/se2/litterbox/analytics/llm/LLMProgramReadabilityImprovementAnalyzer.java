/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.llm;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.llm.ParsedLlmResponseCode;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApi;
import de.uni_passau.fim.se2.litterbox.llm.prompts.PromptBuilder;
import de.uni_passau.fim.se2.litterbox.ast.util.CodeTarget;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

public class LLMProgramReadabilityImprovementAnalyzer extends LLMProgramModificationAnalyzer {
    public LLMProgramReadabilityImprovementAnalyzer(
            IssueTranslator translator,
            CodeTarget target
    ) {
        super(translator, target, false);
    }

    public LLMProgramReadabilityImprovementAnalyzer(
            LlmApi llmApi,
            PromptBuilder promptBuilder,
            CodeTarget target
    ) {
        super(llmApi, promptBuilder, target, false);
    }

    @Override
    public String buildPrompt(Program program) {
        return promptBuilder.improveReadability(program, target);
    }

    @Override
    public Program analyze(Program program) {
        String query = buildPrompt(program);
        ParsedLlmResponseCode llmResponse = scratchLlm.singleQueryWithCodeOnlyResponse(program, query);
        return llmResponseParser.updateProgram(program, llmResponse);
    }
}
