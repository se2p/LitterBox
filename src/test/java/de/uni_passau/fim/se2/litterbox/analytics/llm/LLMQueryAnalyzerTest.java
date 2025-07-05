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
package de.uni_passau.fim.se2.litterbox.analytics.llm;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.llm.DummyLlmApi;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApiProvider;
import de.uni_passau.fim.se2.litterbox.llm.prompts.LlmQuery;
import de.uni_passau.fim.se2.litterbox.llm.prompts.QueryTarget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;

class LLMQueryAnalyzerTest implements JsonTest {

    @Test
    void testAnalyze(@TempDir final Path outputDir) throws IOException {
        final Path inputFile = Path.of("src/test/fixtures/movesteps.json");
        final Path outputFile = outputDir.resolve("movesteps.txt");

        try (var mockStatic = Mockito.mockStatic(LlmApiProvider.class)) {
            DummyLlmApi llmApi = new DummyLlmApi("dummy response");
            mockStatic.when(LlmApiProvider::get).thenReturn(llmApi);

            LLMQueryAnalyzer analyzer = new LLMQueryAnalyzer(
                    outputFile,
                    false,
                    new LlmQuery.CustomQuery("custom user question"),
                    new QueryTarget.ProgramTarget(),
                    false
            );

            analyzer.checkAndWrite(inputFile.toFile());

            final String output = Files.readString(outputFile);
            assertThat(output).contains("dummy response");
        }
    }
}
