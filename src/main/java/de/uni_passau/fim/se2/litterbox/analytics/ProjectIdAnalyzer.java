/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.utils.Downloader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class ProjectIdAnalyzer<R> {
    private static final Logger log = Logger.getLogger(ProjectIdAnalyzer.class.getName());

    private final FileAnalyzer<R> analyzer;
    private final Path projectDir;
    private final boolean delete;

    public ProjectIdAnalyzer(final FileAnalyzer<R> analyzer, final Path projectDir, final boolean delete) {
        this.analyzer = analyzer;
        this.projectDir = projectDir;
        this.delete = delete;
    }

    /**
     * Analyze multiple files based on a list of project ids in the given file.
     *
     * @param projectList is the path to a file containing all the ids of projects that should be analyzed.
     */
    public final void analyzeMultiple(final Path projectList) {
        try (Stream<String> lines = Files.lines(projectList)) {
            List<String> pids = lines.toList();
            for (String pid : pids) {
                analyzeSingle(pid);
            }
        } catch (IOException e) {
            log.warning("Could not read project list at " + projectList);
        }
    }

    /**
     * Analyzes a single project based on the given project id.
     *
     * <p>If a file with the given project id exists in the path with which this analyzer was initialized, the project
     * will be directly analyzed, otherwise it will be downloaded to the configured path and analyzed afterwards.</p>
     *
     * @param pid is the id of the project that should be analyzed.
     */
    public final void analyzeSingle(final String pid) throws IOException {
        Path path = projectDir.resolve(pid + ".json");
        File projectFile = path.toFile();
        if (!projectFile.exists()) {
            try {
                Downloader.downloadAndSaveProject(pid, projectDir);
            } catch (IOException e) {
                log.warning("Could not download project with PID: " + pid);
                return;
            }
        }

        analyzer.checkAndWrite(projectFile);
        deleteFile(projectFile);
    }

    private void deleteFile(final File file) {
        if (!delete) {
            return;
        }

        boolean success = file.delete();
        if (!success) {
            log.warning("Could not delete project: " + file.getName());
        }
    }
}
