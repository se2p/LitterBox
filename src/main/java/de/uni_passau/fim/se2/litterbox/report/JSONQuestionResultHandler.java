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

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import de.uni_passau.fim.se2.litterbox.analytics.questions.ProgramDimension;
import de.uni_passau.fim.se2.litterbox.analytics.questions.ScopeOfFocus;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Outputs questions as JSON with block model metadata.
 *
 * <p>Supports two modes of operation:
 * <ul>
 *   <li><b>File mode:</b> When output is a file path (has .json extension), all projects
 *       are accumulated and written to a single file in {@link #finish()}.</li>
 *   <li><b>Directory mode:</b> When output is a directory (exists as directory or has no
 *       recognized extension), each project is written immediately to a separate file
 *       named {@code <projectname>_questions.json}.</li>
 * </ul>
 */
public class JSONQuestionResultHandler implements QuestionResultHandler {

    private static final Logger log = Logger.getLogger(JSONQuestionResultHandler.class.getName());

    private final Path output;
    private final boolean showAnswers;
    private final IssueTranslator translator;
    private final JsonMapper mapper;
    private final List<ProjectQuestions> projects;
    private final boolean directoryMode;

    public JSONQuestionResultHandler(IssueTranslator translator, Path output, boolean showAnswers) {
        this.output = output;
        this.showAnswers = showAnswers;
        this.translator = translator;
        this.mapper = JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();
        this.projects = new java.util.ArrayList<>();
        this.directoryMode = isDirectoryMode(output);
    }

    private static boolean isDirectoryMode(Path path) {
        if (path == null) {
            return false;
        }
        // If it already exists as a directory, use directory mode
        if (Files.isDirectory(path)) {
            return true;
        }
        // If the filename has no recognized extension, treat as directory
        String filename = path.getFileName().toString();
        return QuestionExportFormat.fromFilename(filename) == null;
    }

    @Override
    public void begin() {
        // Nothing to do
    }

    @Override
    public void handleResult(Path projectFile, Program program, List<QuestionDTO> questions) {
        String projectName = projectFile.getFileName().toString();
        Map<String, Map<String, Integer>> coverage = createCoverageMap(questions);

        // Transform questions if necessary (e.g. to handle null/empty logic for choices/answers)
        // For now, we assume QuestionDTO's default serialization is acceptable or handled by
        // annotations/mixins if needed.
        // But to strictly match previous behavior (null for empty), we might need to recreate DTOs.
        // Let's use the DTOs as is for now, but ensure we respect the showAnswers flag.

        List<JsonQuestionDTO> processedQuestions = questions.stream()
                .map(q -> {
                    List<String> choices = q.getChoices(translator);
                    List<String> answers = q.getCorrectAnswers(translator);
                    return new JsonQuestionDTO(
                            q.id(), q.finder(), q.name(), q.questionType(),
                            q.programDimension(), q.scopeOfFocus(), q.sprite(),
                            q.questionText(),
                            (choices == null || choices.isEmpty()) ? null : choices,
                            (showAnswers && answers != null && !answers.isEmpty()) ? answers : null,
                            q.scratchBlocksCode(),
                            q.hint()
                    );
                })
                .toList();

        ProjectQuestions projectQuestions = new ProjectQuestions(projectName, coverage, processedQuestions);

        if (directoryMode) {
            // Write immediately for this project
            writeProjectToFile(projectFile, projectQuestions);
        } else {
            // Accumulate for single file output
            projects.add(projectQuestions);
        }
    }

    private void writeProjectToFile(Path projectFile, ProjectQuestions projectQuestions) {
        try {
            Files.createDirectories(output);
            String projectName = getProjectBaseName(projectFile);
            Path outputPath = output.resolve(projectName + "_questions.json");

            QuestionExportRoot root = new QuestionExportRoot(List.of(projectQuestions));

            mapper.writeValue(outputPath.toFile(), root);
            log.info("Questions written to: " + outputPath);
        } catch (IOException e) {
            log.severe("Failed to write JSON output for " + projectFile + ": " + e.getMessage());
        }
    }

    private String getProjectBaseName(Path projectFile) {
        String fileName = projectFile.getFileName().toString();
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }

    private Map<String, Map<String, Integer>> createCoverageMap(List<QuestionDTO> questions) {
        // Count questions per cell
        Map<String, Integer> cellCounts = new LinkedHashMap<>();
        for (QuestionDTO q : questions) {
            String cell = q.getBlockModelCell();
            cellCounts.merge(cell, 1, Integer::sum);
        }

        // Create matrix
        Map<String, Map<String, Integer>> coverage = new LinkedHashMap<>();
        for (ProgramDimension dim : ProgramDimension.values()) {
            Map<String, Integer> dimMap = new LinkedHashMap<>();
            for (ScopeOfFocus scope : ScopeOfFocus.values()) {
                String cell = dim.name() + "/" + scope.name();
                dimMap.put(scope.name(), cellCounts.getOrDefault(cell, 0));
            }
            coverage.put(dim.name(), dimMap);
        }

        return coverage;
    }

    @Override
    public void finish() throws IOException {
        if (directoryMode) {
            // In directory mode, files are written in handleResult()
            return;
        }

        QuestionExportRoot root = new QuestionExportRoot(projects);

        try {
            if (output.getParent() != null) {
                Files.createDirectories(output.getParent());
            }
            mapper.writeValue(output.toFile(), root);
            log.info("Questions written to: " + output);
        } catch (IOException e) {
            log.severe("Failed to write JSON output: " + e.getMessage());
            throw e;
        }
    }

    private record QuestionExportRoot(List<ProjectQuestions> projects) {
    }

    private record ProjectQuestions(
            String project,
            Map<String, Map<String, Integer>> blockModelCoverage,
            List<JsonQuestionDTO> questions
    ) {
    }

    /**
     * DTO for JSON output with resolved string fields (not TranslatableText).
     */
    private record JsonQuestionDTO(
            int id,
            String finder,
            String name,
            de.uni_passau.fim.se2.litterbox.analytics.questions.QuestionType questionType,
            ProgramDimension programDimension,
            ScopeOfFocus scopeOfFocus,
            String sprite,
            String questionText,
            List<String> choices,
            List<String> correctAnswers,
            String scratchBlocksCode,
            String hint
    ) {
    }
}
