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

import de.uni_passau.fim.se2.litterbox.llm.api.LlmApi;
import de.uni_passau.fim.se2.litterbox.llm.prompts.LlmQuery;
import de.uni_passau.fim.se2.litterbox.llm.prompts.PromptBuilder;
import de.uni_passau.fim.se2.litterbox.llm.prompts.QueryTarget;

import java.util.Collections;

public class ScratchLlmConversation {

    private final LlmApi llmApi;
    private final PromptBuilder promptBuilder;

    public ScratchLlmConversation(final LlmApi llmApi, final PromptBuilder promptBuilder) {
        this.llmApi = llmApi;
        this.promptBuilder = promptBuilder;
    }

    public Conversation startConversation() {
        return new Conversation(promptBuilder.systemPrompt(), Collections.emptyList());
    }

    // TODO: methods to continue a conversation

    public Conversation askAbout(Conversation conversation, QueryTarget target, LlmQuery question) {
        // we already have the program as part of the conversation and continue
        // the conversation with a follow-up question
        //
        // use the LLMProgramQueryAnalyzer to ask a question and append the
        // output as message to the conversation
        // ...
        throw new UnsupportedOperationException("not implemented");
    }

    public Conversation improve(Conversation conversation, QueryTarget target, String detectors, boolean ignoreLooseBlocks) {
        // ...
        throw new UnsupportedOperationException("not implemented");
    }
}
