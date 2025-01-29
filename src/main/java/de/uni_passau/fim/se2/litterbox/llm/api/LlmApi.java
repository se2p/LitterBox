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
package de.uni_passau.fim.se2.litterbox.llm.api;

import de.uni_passau.fim.se2.litterbox.llm.Conversation;

public interface LlmApi {
    /**
     * Initiates a new conversion by sending a message to the large language model.
     *
     * @param message Some message by the user.
     * @return A new conversation including the user message and the response by the LLM.
     */
    Conversation query(String message);

    /**
     * Initiates a new conversion by sending a message to the large language model.
     *
     * @param systemPrompt A system prompt to be sent to the LLM before the user message.
     * @param message Some message by the user.
     * @return A new conversation including the user message and the response by the LLM.
     */
    Conversation query(String systemPrompt, String message);

    /**
     * Continues an existing conversation.
     *
     * @param conversation Some interaction sequence between user and LLM.
     * @return The updated conversation with the latest response by the LLM appended.
     */
    Conversation query(Conversation conversation);
}
