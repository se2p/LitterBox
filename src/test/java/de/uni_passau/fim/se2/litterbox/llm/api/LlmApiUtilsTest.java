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
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LlmApiUtilsTest {

    @Test
    void testConversationToChatMessagesWithSystemPrompt() {
        String systemPrompt = "You are a helpful assistant.";
        List<LlmMessage> messages = List.of(
                new LlmMessage.GenericLlmMessage("Hello", LlmMessageSender.USER),
                new LlmMessage.GenericLlmMessage("Hi there!", LlmMessageSender.MODEL)
        );
        Conversation conversation = new Conversation(systemPrompt, messages);

        List<ChatMessage> chatMessages = LlmApiUtils.conversationToChatMessages(conversation);

        assertEquals(3, chatMessages.size());
        assertInstanceOf(SystemMessage.class, chatMessages.get(0));
        assertEquals(systemPrompt, ((SystemMessage) chatMessages.get(0)).text());
        assertInstanceOf(UserMessage.class, chatMessages.get(1));
        assertInstanceOf(AiMessage.class, chatMessages.get(2));
    }

    @Test
    void testConversationToChatMessagesWithoutSystemPrompt() {
        List<LlmMessage> messages = List.of(
                new LlmMessage.GenericLlmMessage("Hello", LlmMessageSender.USER),
                new LlmMessage.GenericLlmMessage("Hi there!", LlmMessageSender.MODEL)
        );
        Conversation conversation = new Conversation(null, messages);
        List<ChatMessage> chatMessages = LlmApiUtils.conversationToChatMessages(conversation);

        assertEquals(2, chatMessages.size());
        assertInstanceOf(UserMessage.class, chatMessages.get(0));
        assertInstanceOf(AiMessage.class, chatMessages.get(1));
    }

    @Test
    void testConversationToChatMessagesWithEmptyMessages() {
        Conversation conversation = new Conversation(null, List.of());
        List<ChatMessage> chatMessages = LlmApiUtils.conversationToChatMessages(conversation);
        assertTrue(chatMessages.isEmpty());
    }
}
