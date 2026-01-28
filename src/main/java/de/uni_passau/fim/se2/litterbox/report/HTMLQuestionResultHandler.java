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
import de.uni_passau.fim.se2.litterbox.analytics.questions.ScopeOfFocus;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

/**
 * Outputs questions as HTML with scratchblocks rendering.
 */
public class HTMLQuestionResultHandler implements QuestionResultHandler {

    private static final Logger log = Logger.getLogger(HTMLQuestionResultHandler.class.getName());

    private final IssueTranslator translator;
    private final Path output;
    private final boolean showAnswers;
    private final List<ProjectQuestions> allProjects = new ArrayList<>();

    private record ProjectQuestions(String name, List<QuestionDTO> questions) {}

    public HTMLQuestionResultHandler(IssueTranslator translator, Path output, boolean showAnswers) {
        this.translator = translator;
        this.output = output;
        this.showAnswers = showAnswers;
    }

    @Override
    public void begin() {
        // Nothing to do
    }

    @Override
    public void handleResult(Path projectFile, Program program, List<QuestionDTO> questions) {
        allProjects.add(new ProjectQuestions(projectFile.getFileName().toString(), questions));
    }

    @Override
    public void finish() throws IOException {
        if (output.getParent() != null) {
            Files.createDirectories(output.getParent());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(output)) {
            writer.write(generateHTML());
        }
        log.info("Questions written to: " + output);
    }

    private String generateHTML() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("  <meta charset=\"UTF-8\">\n");
        html.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("  <title>Scratch Comprehension Questions</title>\n");
        html.append("  <script src=\"https://scratchblocks.github.io/js/scratchblocks-v3.6-min.js\"></script>\n");
        html.append("  <style>\n");
        html.append(getCSS());
        html.append("  </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("  <h1>Scratch Comprehension Questions</h1>\n");

        for (ProjectQuestions project : allProjects) {
            html.append("  <div class=\"project\">\n");
            html.append("    <h2>").append(escapeHtml(project.name())).append("</h2>\n");
            html.append(generateCoverageMatrix(project.questions()));

            for (QuestionDTO question : project.questions()) {
                html.append(generateQuestionCard(question));
            }
            html.append("  </div>\n");
        }

        html.append("  <script>\n");
        html.append("    scratchblocks.renderMatching('pre.blocks', {style: 'scratch3'});\n");
        html.append("  </script>\n");
        html.append("</body>\n");
        html.append("</html>\n");

        return html.toString();
    }

    private String getCSS() {
        return """
            body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 2rem; }
            h1 { color: #4d97ff; }
            .project { margin-bottom: 2rem; padding: 1rem; border: 1px solid #ddd; border-radius: 8px; }
            .question-card { background: #f9f9f9; padding: 1rem; margin: 1rem 0; border-radius: 8px; border-left: 4px solid #4d97ff; }
            .question-card.TEXT { border-left-color: #4d97ff; }
            .question-card.EXECUTION { border-left-color: #ff8c1a; }
            .question-card.PURPOSE { border-left-color: #40bf4a; }
            .block-model-tag { display: inline-block; padding: 2px 8px; border-radius: 4px; font-size: 0.8rem; font-weight: bold; margin-right: 0.5rem; }
            .tag-TEXT { background: #4d97ff22; color: #4d97ff; }
            .tag-EXECUTION { background: #ff8c1a22; color: #ff8c1a; }
            .tag-PURPOSE { background: #40bf4a22; color: #40bf4a; }
            .question-type { color: #666; font-size: 0.9rem; }
            .choices { margin-top: 0.5rem; }
            .choice { padding: 0.25rem 0.5rem; margin: 0.25rem 0; background: white; border: 1px solid #ddd; border-radius: 4px; }
            .answer { color: #40bf4a; font-weight: bold; }
            .coverage-matrix { margin: 1rem 0; }
            .coverage-matrix table { border-collapse: collapse; }
            .coverage-matrix th, .coverage-matrix td { border: 1px solid #ddd; padding: 0.5rem; text-align: center; }
            .coverage-matrix th { background: #f0f0f0; }
            .cell-0 { background: #ffeeee; }
            .cell-1 { background: #eeffee; }
            .cell-2 { background: #ddffdd; }
            .cell-3 { background: #ccffcc; }
            pre.blocks { background: white; padding: 0.5rem; border-radius: 4px; }
            """;
    }

    private String generateCoverageMatrix(List<QuestionDTO> questions) {
        Map<String, Integer> coverage = new LinkedHashMap<>();
        for (QuestionDTO q : questions) {
            coverage.merge(q.getBlockModelCell(), 1, Integer::sum);
        }

        StringBuilder html = new StringBuilder();
        html.append("    <div class=\"coverage-matrix\">\n");
        html.append("      <table>\n");
        html.append("        <tr><th></th>");
        for (ScopeOfFocus scope : ScopeOfFocus.values()) {
            html.append("<th>").append(scope.name()).append("</th>");
        }
        html.append("</tr>\n");

        for (ProgramDimension dim : ProgramDimension.values()) {
            html.append("        <tr><th>").append(dim.name()).append("</th>");
            for (ScopeOfFocus scope : ScopeOfFocus.values()) {
                int count = coverage.getOrDefault(dim.name() + "/" + scope.name(), 0);
                String cellClass = "cell-" + Math.min(count, 3);
                html.append("<td class=\"").append(cellClass).append("\">").append(count).append("</td>");
            }
            html.append("</tr>\n");
        }
        html.append("      </table>\n");
        html.append("    </div>\n");

        return html.toString();
    }

    private String generateQuestionCard(QuestionDTO question) {
        StringBuilder html = new StringBuilder();
        html.append("    <div class=\"question-card ").append(question.programDimension().name()).append("\">\n");
        html.append("      <span class=\"block-model-tag tag-").append(question.programDimension().name())
                .append("\">[").append(question.programDimension().name()).append("/")
                .append(question.scopeOfFocus().name()).append("]</span>\n");
        html.append("      <span class=\"question-type\">").append(translator.getName(question.finder()))
                .append(" (").append(question.questionType().name().toLowerCase()).append(")</span>\n");
        html.append("      <p><strong>Sprite:</strong> ").append(escapeHtml(question.sprite())).append("</p>\n");
        html.append("      <p><strong>Q:</strong> ").append(escapeHtml(question.questionText())).append("</p>\n");

        if (question.scratchBlocksCode() != null && !question.scratchBlocksCode().isEmpty()) {
            String code = question.scratchBlocksCode()
                    .replace("[scratchblocks]", "")
                    .replace("[/scratchblocks]", "")
                    .replace("[sbi]", "")
                    .replace("[/sbi]", "")
                    .replace("[var]", "")
                    .replace("[/var]", "")
                    .trim();
            if (!code.isEmpty()) {
                html.append("      <pre class=\"blocks\">").append(escapeHtml(code)).append("</pre>\n");
            }
        }

        if (question.choices() != null && !question.choices().isEmpty()) {
            html.append("      <div class=\"choices\">\n");
            for (String choice : question.choices()) {
                String cleanChoice = cleanScratchBlocksNotation(choice);
                boolean isAnswer = showAnswers && question.correctAnswers() != null
                        && question.correctAnswers().contains(choice);
                html.append("        <div class=\"choice")
                        .append(isAnswer ? " answer" : "").append("\">")
                        .append(escapeHtml(cleanChoice))
                        .append(isAnswer ? " ✓" : "").append("</div>\n");
            }
            html.append("      </div>\n");
        }

        html.append("    </div>\n");
        return html.toString();
    }

    private String cleanScratchBlocksNotation(String text) {
        return text.replace("[sbi]", "")
                .replace("[/sbi]", "")
                .replace("[var]", "")
                .replace("[/var]", "")
                .replace("[scratchblocks]", "")
                .replace("[/scratchblocks]", "")
                .trim();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
