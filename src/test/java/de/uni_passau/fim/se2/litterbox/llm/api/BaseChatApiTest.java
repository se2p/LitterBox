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
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;

class BaseChatApiTest {

    @Test
    void initConversationWithoutSystemPrompt() {
        final DummyChatApi api = new DummyChatApi();
        final Conversation conversation = api.query("initial user message");

        assertThat(conversation.systemPrompt()).isNull();
        assertThat(conversation.messages()).hasSize(2);
        assertThat(conversation.messages()).containsExactly(
                new LlmMessage.GenericLlmMessage("initial user message", LlmMessageSender.USER),
                new LlmMessage.GenericLlmMessage("model message", LlmMessageSender.MODEL)
        ).inOrder();
    }

    @Test
    void initConversationWithSystemPrompt() {
        final DummyChatApi api = new DummyChatApi();
        final Conversation conversation = api.query("system prompt", "initial user message");

        assertThat(conversation.systemPrompt()).isEqualTo("system prompt");
        assertThat(conversation.messages()).hasSize(2);
        assertThat(conversation.messages()).containsExactly(
                new LlmMessage.GenericLlmMessage("initial user message", LlmMessageSender.USER),
                new LlmMessage.GenericLlmMessage("model message", LlmMessageSender.MODEL)
        ).inOrder();
    }

    @Test
    void continueConversation() {
        final DummyChatApi api = new DummyChatApi();
        final Conversation conversation = new Conversation("system prompt", List.of(
                new LlmMessage.GenericLlmMessage("initial user message", LlmMessageSender.USER),
                new LlmMessage.GenericLlmMessage("first model message", LlmMessageSender.MODEL)
        ));

        final Conversation newConversation = api.query(conversation);
        assertThat(newConversation.systemPrompt()).isEqualTo(conversation.systemPrompt());
        assertThat(newConversation.messages()).hasSize(conversation.messages().size() + 1);
        assertThat(newConversation.messages()).containsExactly(
                new LlmMessage.GenericLlmMessage("initial user message", LlmMessageSender.USER),
                new LlmMessage.GenericLlmMessage("first model message", LlmMessageSender.MODEL),
                new LlmMessage.GenericLlmMessage("model message", LlmMessageSender.MODEL)
        ).inOrder();
    }

    private static class DummyChatApi extends BaseChatApi {

        @Override
        protected ChatModel buildModel() {
            return new ChatModel() {
                @Override
                public ChatResponse chat(ChatRequest chatRequest) {
                    return ChatResponse.builder()
                            .modelName("dummy")
                            .aiMessage(new AiMessage("model message"))
                            .build();
                }
            };
        }
    }
}
