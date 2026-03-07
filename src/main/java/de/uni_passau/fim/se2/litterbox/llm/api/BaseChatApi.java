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
package de.uni_passau.fim.se2.litterbox.llm.api;

import de.uni_passau.fim.se2.litterbox.llm.Conversation;
import de.uni_passau.fim.se2.litterbox.llm.LlmMessage;
import de.uni_passau.fim.se2.litterbox.llm.LlmMessageSender;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.exception.*;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public abstract class BaseChatApi implements LlmApi {

    private static final Logger log = Logger.getLogger(BaseChatApi.class.getName());

    protected final int maxRequestRetries;

    protected final Duration requestRetryInterval;

    protected final ChatModel model;

    protected BaseChatApi() {
        this(3, Duration.ofSeconds(30));
    }

    protected BaseChatApi(final int maxRequestRetries, final Duration requestRetryInterval) {
        Preconditions.checkArgument(maxRequestRetries >= 1, "LLM request retries must be >= 1");
        Preconditions.checkArgument(
                requestRetryInterval.isPositive(), "LLM request retry interval must be > 0."
        );

        this.model = buildModel();
        this.maxRequestRetries = maxRequestRetries;
        this.requestRetryInterval = requestRetryInterval;
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

        final ChatResponse response = tryChat(queryMessages, 1);

        final List<LlmMessage> messages = new ArrayList<>();
        messages.add(new LlmMessage.GenericLlmMessage(message, LlmMessageSender.USER));
        messages.add(new LlmMessage.GenericLlmMessage(response.aiMessage().text(), LlmMessageSender.MODEL));

        return new Conversation(systemPrompt, messages);
    }

    @Override
    public Conversation query(final Conversation conversation) {
        final ChatResponse response = tryChat(LlmApiUtils.conversationToChatMessages(conversation), 1);
        final LlmMessage msg = new LlmMessage.GenericLlmMessage(response.aiMessage().text(), LlmMessageSender.MODEL);

        return conversation.add(msg);
    }

    protected ChatResponse tryChat(final List<ChatMessage> queryMessages, final int attempt) {
        if (attempt > maxRequestRetries) {
            throw new LitterBoxLlmException("LLM API requests failed even after multiple tries. "
                    + "Check your network connection and/or if the API service is reachable.");
        }

        // expected exceptions from dev.langchain4j.internal.ExceptionMapper.DefaultExceptionMapper
        // some of them we can handle by retrying the request
        try {
            return model.chat(queryMessages);
        } catch (RateLimitException rateLimitException) {
            log.info(
                    "LLM API request was rate limited. Retrying in " + requestRetryInterval.toSeconds() + "seconds."
            );
            trySleep();
        } catch (TimeoutException timeoutException) {
            log.info("LLM API request timed out. Retrying in " + requestRetryInterval.toSeconds() + "seconds.");
            trySleep();
        } catch (AuthenticationException authenticationException) {
            throw new LitterBoxLlmException(
                    "Invalid LLM API key. Please check your LitterBox configuration.",
                    authenticationException
            );
        } catch (ModelNotFoundException modelNotFoundException) {
            throw new LitterBoxLlmException(
                    "Requested LLM model could not be found. Please check your LitterBox configuration.",
                    modelNotFoundException
            );
        } catch (Exception e) {
            throw new LitterBoxLlmException(
                    "LitterBox experienced an unexpected error when trying to access the LLM API.",
                    e
            );
        }

        return tryChat(queryMessages, attempt + 1);
    }

    private void trySleep() {
        try {
            Thread.sleep(requestRetryInterval);
        } catch (InterruptedException e) {
            // not much we can do, ignore
        }
    }
}
