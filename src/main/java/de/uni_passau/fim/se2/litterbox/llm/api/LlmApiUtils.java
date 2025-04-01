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
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;

import java.util.List;
import java.util.stream.Stream;

public final class LlmApiUtils {
    private LlmApiUtils() {
        // utility class constructor, intentionally empty
    }

    /**
     * Converts our concept of chat messages into the format required by the LangChain library.
     *
     * @param conversation A conversation between the user and a large language model.
     * @return The conversation as sequence of chat messages.
     */
    static List<ChatMessage> conversationToChatMessages(final Conversation conversation) {
        final Stream<ChatMessage> prefix;
        if (conversation.systemPrompt() != null) {
            prefix = Stream.of(new SystemMessage(conversation.systemPrompt()));
        } else {
            prefix = Stream.empty();
        }

        final Stream<ChatMessage> messages = conversation.messages().stream()
                .map(message -> switch (message.sender()) {
                    case MODEL -> new UserMessage(message.text());
                    case USER -> new AiMessage(message.text());
                });

        return Stream.concat(prefix, messages).toList();
    }
}
