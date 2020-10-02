/*
 * Copyright (C) 2020 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;
import de.uni_passau.fim.se2.litterbox.utils.Downloader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class Analyzer {

    private static final Logger log = Logger.getLogger(Analyzer.class.getName());

    Path input;
    String output;

    public Analyzer(String input, String output) {
        this.input = Paths.get(input);
        this.output = output;
    }

    /**
     * Analyzes the file or directory this analyzer was initialized with.
     *
     * <p>If the input is a file it will be directly analyzed, if it is a director all files in the
     * directory will be analyzed one after another.</p>
     */
    public void analyzeFile() {
        File file = input.toFile();

        if (file.exists() && file.isDirectory()) {
            for (final File fileEntry : Objects.requireNonNull(file.listFiles())) {
                if (!fileEntry.isDirectory()) {
                    check(fileEntry, output);
                }
            }
        } else if (file.exists() && !file.isDirectory()) {
            check(file, output);
        } else {
            log.info("Folder or file '" + file.getName() + "' does not exist");
        }
    }

    /**
     * Analzed multiple files based on a list of projectids in the given file.
     *
     * @param listPath is the path to a file containing all the ids of projects that should be analyzed.
     */
    public void analyzeMultiple(String listPath) {
        Path projectList = Paths.get(listPath);

        try {
            List<String> pids = Files.lines(projectList).collect(Collectors.toList());
            for (String pid : pids) {
                analyzeSingle(pid);
            }
        } catch (IOException e) {
            log.warning("Could not read project list at " + projectList.toString());
        }
    }

    /**
     * Analyzes a single project based on the given project id.
     *
     * <p>If a file with the given projectid exists in the path with which this analyzer was initialized, the project
     * will be directly analyzed, otherwise it will be downloaded to the configured path and analyzed afterwards.</p>
     *
     * @param pid is the id of the project that should be analyzed.
     */
    public void analyzeSingle(String pid) {
        Path path = Paths.get(input.toString(), pid + ".json");
        File projectFile = path.toFile();
        if (!projectFile.exists()) {
            try {
                Downloader.downloadAndSaveProject(pid, input.toString());
            } catch (IOException e) {
                log.warning("[Error] Could not download project with PID: " + pid);
                return;
            }
        }

        check(projectFile, output);
    }

    abstract void check(File fileEntry, String csv);

    /**
     * Extracts a Scratch Program from a Json or sb3 file.
     *
     * @param fileEntry of the json or sb3 file
     * @return the parsed program or null in case the program could not be loaded or parsed
     */
    protected Program extractProgram(File fileEntry) {
        Scratch3Parser parser = new Scratch3Parser();
        Program program = null;
        try {
            program = parser.parseFile(fileEntry);
        } catch (IOException e) {
            log.info("[Error] could not load program from file " + fileEntry.getName());
        } catch (ParsingException | RuntimeException e) {
            // TODO: Proper error handling
            log.info("[Error] could not parse program for file " + fileEntry.getName());
        }
        return program;
    }
}
