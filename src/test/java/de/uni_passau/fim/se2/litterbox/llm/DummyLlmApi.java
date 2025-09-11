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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DummyLlmApi implements LlmApi {

    private final List<String> requests = new ArrayList<>();

    private final List<String> responses;

    private int state = 0;

    public DummyLlmApi(final String... responses) {
        this.responses = Arrays.asList(responses);
    }

    public DummyLlmApi(final List<String> response) {
        this.responses = response;
    }

    @Override
    public Conversation query(String message) {
        requests.add(message);

        return new Conversation(
                "",
                List.of(
                        new LlmMessage.GenericLlmMessage(message, LlmMessageSender.USER),
                        new LlmMessage.GenericLlmMessage(nextResponse(), LlmMessageSender.MODEL)
                )
        );
    }

    @Override
    public Conversation query(String systemPrompt, String message) {
        requests.add(message);

        return new Conversation(
                systemPrompt,
                List.of(
                        new LlmMessage.GenericLlmMessage(message, LlmMessageSender.USER),
                        new LlmMessage.GenericLlmMessage(nextResponse(), LlmMessageSender.MODEL)
                )
        );
    }

    @Override
    public Conversation query(Conversation conversation) {
        return conversation.add(new LlmMessage.GenericLlmMessage(nextResponse(), LlmMessageSender.MODEL));
    }

    public List<String> getRequests() {
        return Collections.unmodifiableList(requests);
    }

    private String nextResponse() {
        return responses.get(state++);
    }
}
