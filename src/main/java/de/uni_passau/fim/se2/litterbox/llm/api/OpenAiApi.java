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
import de.uni_passau.fim.se2.litterbox.llm.LlmMessage;
import de.uni_passau.fim.se2.litterbox.llm.LlmMessageSender;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;

import java.util.ArrayList;
import java.util.List;

public class OpenAiApi implements LlmApi {

    private final OpenAiChatModel model;

    public OpenAiApi() {
        model = OpenAiChatModel.builder()
                .baseUrl(System.getProperty("litterbox.llm.openai.base-url"))
                .apiKey(System.getProperty("litterbox.llm.openai.api-key"))
                .modelName(System.getProperty("litterbox.llm.openai.model"))
                .build();
    }

    @Override
    public Conversation query(final String message) {
        return query(null, message);
    }

    @Override
    public Conversation query(String systemPrompt, String message) {
        final UserMessage userMessage = new UserMessage(message);

        final List<ChatMessage> queryMessages;
        if (systemPrompt == null) {
            queryMessages = List.of(userMessage);
        } else {
            final SystemMessage systemMessage = new SystemMessage(systemPrompt);
            queryMessages = List.of(systemMessage, userMessage);
        }

        final ChatResponse response = model.chat(queryMessages);

        final List<LlmMessage> messages = new ArrayList<>();
        messages.add(new LlmMessage.GenericLlmMessage(message, LlmMessageSender.USER));
        messages.add(new LlmMessage.GenericLlmMessage(response.aiMessage().text(), LlmMessageSender.MODEL));

        return new Conversation(systemPrompt, messages);
    }

    @Override
    public Conversation query(final Conversation conversation) {
        final ChatResponse response = model.chat(LlmApiUtils.conversationToChatMessages(conversation));
        final LlmMessage msg = new LlmMessage.GenericLlmMessage(response.aiMessage().text(), LlmMessageSender.MODEL);

        return conversation.add(msg);
    }
}
