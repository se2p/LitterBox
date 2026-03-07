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

/**
 * Export formats for quiz/questions output.
 */
public enum QuestionExportFormat {
    /**
     * Console output (default when no output file specified).
     */
    CONSOLE("Console output"),

    /**
     * JSON format for web frontends.
     */
    JSON("JSON format"),

    /**
     * Rich HTML report with interactive features and scratchblocks rendering.
     */
    HTML("HTML report");

    private final String description;

    QuestionExportFormat(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns the file extension for this format, including the dot.
     *
     * @return The file extension (e.g., ".json", ".html"), or null for CONSOLE
     */
    public String getFileExtension() {
        return switch (this) {
            case JSON -> ".json";
            case HTML -> ".html";
            case CONSOLE -> null;
        };
    }

    /**
     * Infers the export format from a file extension.
     *
     * @param filename The output filename
     * @return The inferred format, or null if unknown
     */
    public static QuestionExportFormat fromFilename(String filename) {
        if (filename == null) {
            return CONSOLE;
        }
        String lower = filename.toLowerCase();
        if (lower.endsWith(".json")) {
            return JSON;
        } else if (lower.endsWith(".html") || lower.endsWith(".htm")) {
            return HTML;
        }
        return null;
    }
}
