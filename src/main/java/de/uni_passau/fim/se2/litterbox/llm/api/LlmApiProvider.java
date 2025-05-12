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

public final class LlmApiProvider {

    private static final String LLM_API_PROPERTY = "litterbox.llm.api";

    private LlmApiProvider() {
        // intentionally empty
    }

    public static LlmApi get() {
        final String apiType = System.getProperty(LLM_API_PROPERTY, "openai");
        return switch (apiType) {
            case "openai" -> new OpenAiApi();
            case "ollama" -> new OllamaApi();
            default -> new OpenAiApi();
        };
    }
}
