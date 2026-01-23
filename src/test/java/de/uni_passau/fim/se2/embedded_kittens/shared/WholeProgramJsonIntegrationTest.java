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
package de.uni_passau.fim.se2.embedded_kittens.shared;

import static com.google.common.truth.Truth.assertThat;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.uni_passau.fim.se2.embedded_kittens.CliTest;

class WholeProgramJsonIntegrationTest extends CliTest {

    private static final String GGNN_CMD = "ggnn";
    private static final String INPUT_FLAG = "-p";
    private static final String WHOLE_PROGRAM_FLAG = "--whole-program-json";

    @Test
    void noWholeProgramAndWholeProgramJsonAllowed() {
        int exitCode = commandLine.execute(
            GGNN_CMD, INPUT_FLAG, "src/test/fixtures/multipleSprites.json", WHOLE_PROGRAM_FLAG,
            "--whole-program"
        );
        assertThat(exitCode).isEqualTo(2);
        assertStdErrContains("are mutually exclusive");
    }

    @Test
    void ggnnProcessProgramWithMultipleSprites() {
        commandLine.execute(
            GGNN_CMD, INPUT_FLAG, "src/test/fixtures/multipleSprites.json", WHOLE_PROGRAM_FLAG
        );
        assertEmptyStdErr();

        final String output = getOutput();
        assertThat(output).contains("""
            "programIdentifier":"multipleSprites"
            """.strip());
        assertThat(StringUtils.countMatches(output, "\"graph\"")).isEqualTo(3);
    }

    @ParameterizedTest
    @ValueSource(strings = { GGNN_CMD, "astnn", "code2vec", "code2seq", "tokenizer" })
    void processProgramWithMultipleSprites(final String modelParam) {
        commandLine.execute(
            modelParam, INPUT_FLAG, "src/test/fixtures/multipleSprites.json", WHOLE_PROGRAM_FLAG
        );
        assertEmptyStdErr();

        final String output = getOutput();
        assertThat(output).contains("""
            "programIdentifier":"multipleSprites"
            """.strip());
        assertThat(output).contains("abby");
        assertThat(output).contains("cat");
        assertThat(output).contains("stage");
    }
}
