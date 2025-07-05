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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;

class LlmApiProviderTest {

    @Test
    void getDefault() {
        assertThat(LlmApiProvider.get()).isInstanceOf(OpenAiApi.class);
    }

    @ParameterizedTest
    @MethodSource("contextLengths")
    void getOllama(final String contextLength) {
        final String prevProp = System.setProperty(LlmApiProvider.LLM_API_PROPERTY, "ollama");
        System.setProperty("litterbox.llm.ollama.base-url", "http://localhost:11434");
        System.setProperty("litterbox.llm.ollama.model", "gemma3");
        if (contextLength != null) {
            System.setProperty("litterbox.llm.ollama.num-context", contextLength);
        }

        assertThat(LlmApiProvider.get()).isInstanceOf(OllamaApi.class);

        if (prevProp != null) {
            System.setProperty(LlmApiProvider.LLM_API_PROPERTY, prevProp);
        }
    }

    private static Stream<Arguments> contextLengths() {
        return Stream.of(
                Arguments.of(new Object[]{null}),
                Arguments.of(""),
                Arguments.of("8192")
        );
    }
}
