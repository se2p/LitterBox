package de.uni_passau.fim.se2.litterbox.llm.api;

import de.uni_passau.fim.se2.litterbox.llm.Conversation;
import de.uni_passau.fim.se2.litterbox.llm.LlmMessage;
import de.uni_passau.fim.se2.litterbox.llm.LlmMessageSender;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseChatApi implements LlmApi {
    protected final ChatModel model;

    protected BaseChatApi() {
        this.model = buildModel();
    }

    protected abstract ChatModel buildModel();

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
