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

import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public record Conversation(String systemPrompt, List<LlmMessage> messages) {

    public Conversation {
        messages = Collections.unmodifiableList(messages);
    }

    public LlmMessage getLast() {
        // should hold for all conversations constructed via a proper LLM interaction, since it contains at
        // least the initial user prompt
        Preconditions.checkArgument(!messages.isEmpty());

        return messages.get(messages.size() - 1);
    }

    public Conversation add(LlmMessage... messages) {
        final List<LlmMessage> msgs = new ArrayList<>(messages());
        msgs.addAll(Arrays.asList(messages));

        return new Conversation(this.systemPrompt(), msgs);
    }
}
