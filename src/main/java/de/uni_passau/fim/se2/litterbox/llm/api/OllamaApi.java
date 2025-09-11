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

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

import java.util.logging.Logger;

public class OllamaApi extends BaseChatApi {

    private static final Logger log = Logger.getLogger(OllamaApi.class.getName());

    @Override
    protected ChatModel buildModel() {
        return OllamaChatModel.builder()
                .baseUrl(System.getProperty("litterbox.llm.ollama.base-url"))
                .numCtx(getNumCtx())
                .modelName(System.getProperty("litterbox.llm.ollama.model"))
                .build();
    }

    private Integer getNumCtx() {
        final String context = System.getProperty("litterbox.llm.ollama.num-context");
        Integer numCtx = null;
        if (context != null) {
            try {
                numCtx = Integer.parseUnsignedInt(context);
            } catch (NumberFormatException e) {
                log.warning("Invalid number of context tokens for Ollama.");
            }
        }

        return numCtx;
    }
}
