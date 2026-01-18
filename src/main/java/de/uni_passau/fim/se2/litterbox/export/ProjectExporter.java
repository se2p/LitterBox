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
package de.uni_passau.fim.se2.litterbox.export;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Abstract base class for exporting Scratch projects to a directory structure.
 *
 * <p>Provides common utilities for:
 * <ul>
 *   <li>ZIP file asset extraction</li>
 *   <li>Filename sanitization</li>
 *   <li>Error collection during export</li>
 * </ul>
 *
 * <p>Subclasses implement format-specific export logic.
 */
public abstract class ProjectExporter {

    private static final Logger log = Logger.getLogger(ProjectExporter.class.getName());

    /** Collects non-fatal errors that occurred during export. */
    protected final List<String> exportErrors = new ArrayList<>();

    /**
     * Exports a program to a project directory.
     *
     * @param sb3File   Path to the source .sb3 file (required for asset extraction)
     * @param program   The parsed program AST
     * @param outputDir The directory to create the project in
     * @throws IOException If a fatal file operation fails
     */
    public abstract void export(Path sb3File, Program program, Path outputDir) throws IOException;

    /**
     * Extracts a single asset from a ZIP file to the target directory.
     *
     * @param zip       The open ZIP file
     * @param entry     The ZIP entry to extract
     * @param targetDir The directory to extract to
     * @return true if extraction succeeded, false otherwise
     */
    protected boolean extractEntry(ZipFile zip, ZipEntry entry, Path targetDir) {
        try (InputStream in = zip.getInputStream(entry)) {
            Path targetPath = targetDir.resolve(Path.of(entry.getName()).getFileName());
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            String error = "Failed to extract asset: " + entry.getName() + " - " + e.getMessage();
            log.warning(error);
            exportErrors.add(error);
            return false;
        }
    }

    /**
     * Extracts an asset from a ZIP file using its md5ext identifier.
     *
     * @param zip         The open ZIP file
     * @param md5ext      The md5 hash with extension (e.g., "abc123.png")
     * @param logicalName The logical name to use for the output file
     * @param targetDir   The directory to extract to
     * @return true if extraction succeeded, false otherwise
     */
    protected boolean extractAsset(ZipFile zip, String md5ext, String logicalName, Path targetDir) {
        if (md5ext == null) {
            String error = "Skipping asset extraction: md5ext is null for " + logicalName;
            log.warning(error);
            exportErrors.add(error);
            return false;
        }

        ZipEntry entry = zip.getEntry(md5ext);
        if (entry == null) {
            String error = "Asset not found in sb3: " + md5ext;
            log.warning(error);
            exportErrors.add(error);
            return false;
        }

        try (InputStream in = zip.getInputStream(entry)) {
            String extension = getExtension(md5ext);
            String fileName = logicalName;
            if (!logicalName.endsWith(extension)) {
                fileName += extension;
            }
            Path targetPath = targetDir.resolve(fileName);
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            String error = "Failed to extract asset: " + logicalName + " (" + md5ext + ") - " + e.getMessage();
            log.warning(error);
            exportErrors.add(error);
            return false;
        }
    }

    /**
     * Sanitizes a name for use as a filename.
     * Replaces characters that are invalid in filenames with underscores.
     *
     * @param name The name to sanitize
     * @return A filesystem-safe version of the name
     */
    protected String sanitizeFilename(String name) {
        return name.replaceAll("[/\\\\:*?\"<>|]", "_");
    }

    /**
     * Gets the file extension from a filename.
     *
     * @param fileName The filename
     * @return The extension including the dot, or empty string if none
     */
    protected String getExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(dotIndex) : "";
    }

    /**
     * Gets the base name of a file (without extension).
     *
     * @param fileName The filename
     * @return The base name without extension
     */
    protected String getBaseName(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    /**
     * Returns any errors that occurred during the most recent export.
     *
     * @return List of error messages (may be empty)
     */
    public List<String> getExportErrors() {
        return new ArrayList<>(exportErrors);
    }

    /**
     * Clears the error list. Call before starting a new export.
     */
    protected void clearErrors() {
        exportErrors.clear();
    }

    /**
     * Logs a summary of the export operation.
     *
     * @param outputDir The output directory
     */
    protected void logExportSummary(Path outputDir) {
        if (exportErrors.isEmpty()) {
            log.info("Exported project to: " + outputDir);
        } else {
            log.warning("Exported project to: " + outputDir + " with " + exportErrors.size() + " error(s)");
        }
    }
}
