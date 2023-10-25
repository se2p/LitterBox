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

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;
import de.uni_passau.fim.se2.litterbox.utils.Downloader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

public abstract class Analyzer<R> {

    private static final Logger log = Logger.getLogger(Analyzer.class.getName());
    protected final Path output;
    protected final boolean delete;
    private final Scratch3Parser parser = new Scratch3Parser();
    private final Path input;

    protected Analyzer(Path input, Path output, boolean delete) {
        this.input = input;
        this.output = output;
        this.delete = delete;
    }

    /**
     * Analyzes the file or directory this analyzer was initialized with.
     *
     * <p>If the input is a file it will be directly analyzed, if it is a director all files in the
     * directory will be analyzed one after another.</p>
     */
    public final void analyzeFile() throws IOException {
        File file = input.toFile();
        if (file.exists() && file.isDirectory()) {
            List<Path> listOfFiles = getProgramPaths(file.toPath());
            for (Path filePath : listOfFiles) {
                File fileEntry = filePath.toFile();
                checkAndWrite(fileEntry);
                deleteFile(fileEntry);
            }
        } else if (file.exists() && !file.isDirectory()) {
            checkAndWrite(file);
            deleteFile(file);
        } else {
            log.severe("Folder or file '" + file.getName() + "' does not exist");
        }
    }

    private static List<Path> getProgramPaths(Path dirPath) throws IOException {
        try (var files = Files.walk(dirPath, 1)) {
            return files.filter(p -> !Files.isDirectory(p))
                    .filter(Analyzer::isPossibleScratchFile)
                    .toList();
        }
    }

    private void deleteFile(File file) {
        if (delete && isPossibleScratchFile(file.toPath())) {
            boolean success = file.delete();
            if (!success) {
                log.warning("Could not delete project: " + file.getName());
            }
        }
    }

    private static boolean isPossibleScratchFile(final Path path) {
        final String filename = path.getFileName().toString();
        return filename.endsWith(".json") || filename.endsWith(".sb3");
    }

    /**
     * Analyze multiple files based on a list of project ids in the given file.
     *
     * @param projectList is the path to a file containing all the ids of projects that should be analyzed.
     */
    public final void analyzeMultiple(Path projectList) {
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
    public final void analyzeSingle(String pid) throws IOException {
        Path path = input.resolve(pid + ".json");
        File projectFile = path.toFile();
        if (!projectFile.exists()) {
            try {
                Downloader.downloadAndSaveProject(pid, input);
            } catch (IOException e) {
                log.warning("Could not download project with PID: " + pid);
                return;
            }
        }

        checkAndWrite(projectFile);
        deleteFile(projectFile);
    }

    /**
     * First uses {@link #check(File)} to parse and check the program, then writes the result to the output file.
     *
     * <p>In case a subclass can more efficiently combine those two operations, e.g., to avoid (partial) calculation of
     * the check results, it should overwrite this method.
     *
     * @param file The input file that contains the Scratch program.
     * @throws IOException In case either reading the program fails, or writing to the output file fails.
     */
    protected void checkAndWrite(final File file) throws IOException {
        final Program program = extractProgram(file);
        if (program == null) {
            return;
        }

        final R result = check(program);
        writeResultToFile(file.toPath(), program, result);
    }

    public R check(final File file) throws ParsingException {
        Program program = extractProgram(file);
        if (program == null) {
            throw new ParsingException("Program could not be read or parsed");
        }

        return check(program);
    }

    protected abstract void writeResultToFile(Path projectFile, Program program, R checkResult) throws IOException;

    public abstract R check(Program program);

    /**
     * Extracts a Scratch Program from a Json or sb3 file.
     *
     * @param fileEntry of the json or sb3 file
     * @return the parsed program or null in case the program could not be loaded or parsed
     */
    protected final Program extractProgram(File fileEntry) {
        try {
            return parser.parseFile(fileEntry);
        } catch (IOException e) {
            log.severe("Could not load program from file " + fileEntry.getName());
        } catch (ParsingException e) {
            log.severe("Could not parse program for file " + fileEntry.getName() + ". " + e.getMessage());
        } catch (RuntimeException e) {
            log.severe("Could not parse program for file " + fileEntry.getName() + ".");
        }

        return null;
    }
}
