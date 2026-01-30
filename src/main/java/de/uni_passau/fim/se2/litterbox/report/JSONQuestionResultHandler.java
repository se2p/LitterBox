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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.uni_passau.fim.se2.litterbox.analytics.questions.ProgramDimension;
import de.uni_passau.fim.se2.litterbox.analytics.questions.ScopeOfFocus;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

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
    private final ObjectMapper mapper;
    private final ArrayNode projectsArray;
    private final boolean directoryMode;

    public JSONQuestionResultHandler(Path output, boolean showAnswers) {
        this.output = output;
        this.showAnswers = showAnswers;
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.projectsArray = mapper.createArrayNode();
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
        ObjectNode projectNode = mapper.createObjectNode();
        projectNode.put("project", projectFile.getFileName().toString());

        // Add block model coverage
        projectNode.set("blockModelCoverage", createCoverageNode(questions));

        // Add questions array
        ArrayNode questionsArray = mapper.createArrayNode();
        for (QuestionDTO question : questions) {
            questionsArray.add(createQuestionNode(question));
        }
        projectNode.set("questions", questionsArray);

        if (directoryMode) {
            // Write immediately for this project
            writeProjectToFile(projectFile, projectNode);
        } else {
            // Accumulate for single file output
            projectsArray.add(projectNode);
        }
    }

    private void writeProjectToFile(Path projectFile, ObjectNode projectNode) {
        try {
            Files.createDirectories(output);
            String projectName = getProjectBaseName(projectFile);
            Path outputPath = output.resolve(projectName + "_questions.json");

            ObjectNode root = mapper.createObjectNode();
            ArrayNode projects = mapper.createArrayNode();
            projects.add(projectNode);
            root.set("projects", projects);

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

    private ObjectNode createCoverageNode(List<QuestionDTO> questions) {
        ObjectNode coverageNode = mapper.createObjectNode();

        // Count questions per cell
        Map<String, Integer> cellCounts = new LinkedHashMap<>();
        for (QuestionDTO q : questions) {
            String cell = q.getBlockModelCell();
            cellCounts.merge(cell, 1, Integer::sum);
        }

        // Create matrix
        for (ProgramDimension dim : ProgramDimension.values()) {
            ObjectNode dimNode = mapper.createObjectNode();
            for (ScopeOfFocus scope : ScopeOfFocus.values()) {
                String cell = dim.name() + "/" + scope.name();
                dimNode.put(scope.name(), cellCounts.getOrDefault(cell, 0));
            }
            coverageNode.set(dim.name(), dimNode);
        }

        return coverageNode;
    }

    private ObjectNode createQuestionNode(QuestionDTO question) {
        ObjectNode node = mapper.createObjectNode();
        node.put("id", question.id());
        node.put("finder", question.finder());
        node.put("name", question.name());
        node.put("questionType", question.questionType().name());
        node.put("programDimension", question.programDimension().name());
        node.put("scopeOfFocus", question.scopeOfFocus().name());
        node.put("sprite", question.sprite());
        node.put("questionText", question.questionText());

        if (question.choices() != null && !question.choices().isEmpty()) {
            ArrayNode choicesArray = mapper.createArrayNode();
            question.choices().forEach(choicesArray::add);
            node.set("choices", choicesArray);
        } else {
            node.putNull("choices");
        }

        if (showAnswers && question.correctAnswers() != null && !question.correctAnswers().isEmpty()) {
            ArrayNode answersArray = mapper.createArrayNode();
            question.correctAnswers().forEach(answersArray::add);
            node.set("correctAnswers", answersArray);
        } else {
            node.putNull("correctAnswers");
        }

        if (question.scratchBlocksCode() != null) {
            node.put("scratchBlocksCode", question.scratchBlocksCode());
        }

        if (question.hint() != null) {
            node.put("hint", question.hint());
        }

        return node;
    }

    @Override
    public void finish() throws IOException {
        if (directoryMode) {
            // In directory mode, files are written in handleResult()
            return;
        }

        ObjectNode root = mapper.createObjectNode();
        root.set("projects", projectsArray);

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
}
