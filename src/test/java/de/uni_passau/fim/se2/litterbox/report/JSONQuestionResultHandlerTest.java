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
package de.uni_passau.fim.se2.litterbox.report;

import de.uni_passau.fim.se2.litterbox.analytics.questions.ProgramDimension;
import de.uni_passau.fim.se2.litterbox.analytics.questions.QuestionType;
import de.uni_passau.fim.se2.litterbox.analytics.questions.ScopeOfFocus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JSONQuestionResultHandlerTest {

    @TempDir
    Path tempDir;

    @Test
    void testFileModeOutput() throws IOException {
        Path outputFile = tempDir.resolve("questions.json");
        JSONQuestionResultHandler handler = new JSONQuestionResultHandler(outputFile, true);

        QuestionDTO q1 = new QuestionDTO(
                1, "Finder", "Type", QuestionType.FREE_TEXT,
                ProgramDimension.PURPOSE, ScopeOfFocus.ATOM, "Sprite1",
                "Question Text", null, List.of("Answer"), "blocks", "hint"
        );

        handler.handleResult(Path.of("project1.sb3"), null, List.of(q1));
        handler.finish();

        assertTrue(Files.exists(outputFile), "Output file should exist");
        String content = Files.readString(outputFile);
        assertTrue(content.contains("\"project\" : \"project1.sb3\""), "Should contain project name");
        assertTrue(content.contains("\"questions\" : ["), "Should contain questions array");
        assertTrue(content.contains("\"id\" : 1"), "Should contain question ID");
        assertTrue(content.contains("\"correctAnswers\" : [ \"Answer\" ]"), "Should contain answers");
    }

    @Test
    void testDirectoryModeOutput() throws IOException {
        Path outputDir = tempDir.resolve("output_dir");
        Files.createDirectories(outputDir);

        // When output is a directory, it uses directory mode
        JSONQuestionResultHandler handler = new JSONQuestionResultHandler(outputDir, false);

        QuestionDTO q1 = new QuestionDTO(
                1, "Finder", "Type", QuestionType.FREE_TEXT,
                ProgramDimension.PURPOSE, ScopeOfFocus.ATOM, "Sprite1",
                "Question Text", null, null, null, null
        );

        handler.handleResult(Path.of("project2.sb3"), null, List.of(q1));
        handler.finish();

        Path expectedFile = outputDir.resolve("project2_questions.json");
        assertTrue(Files.exists(expectedFile), "Output file should exist in directory");
        String content = Files.readString(expectedFile);
        assertTrue(content.contains("\"project\" : \"project2.sb3\""), "Should contain project name");
        assertTrue(content.contains("\"correctAnswers\" : null"), "Should have null answers when showAnswers=false");
    }
}
