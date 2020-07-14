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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import de.uni_passau.fim.se2.litterbox.utils.Downloader;
import de.uni_passau.fim.se2.litterbox.utils.JsonParser;
import de.uni_passau.fim.se2.litterbox.utils.ZipReader;
import org.apache.commons.io.FilenameUtils;

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

    public void analyzeMultiple(String listPath) {
        Path projectList = Paths.get(listPath);

        try {
            List<String> pids = Files.lines(projectList).collect(Collectors.toList());
            for (String pid : pids) {
                analyzeSingle(pid);
            }
        } catch (IOException e) {
            log.warning("Could not read project list at " + projectList.toString());
            e.printStackTrace();
        }
    }

    public void analyzeSingle(String pid) {
        Path path = Paths.get(input.toString(), pid + ".json");
        File projectFile = path.toFile();
        if (!projectFile.exists()) {
            try {
                Downloader.downloadAndSaveProject(pid, input.toString());
            } catch (IOException e) {
                log.warning("Could not download project with PID: " + pid);
                e.printStackTrace();
            }
        }

        check(projectFile, output);
    }

    abstract void check(File fileEntry, String csv);

    /**
     * Extracts a Scratch Program from a Json or sb3 file
     *
     * @param fileEntry of the json or sb3 file
     * @return the parsed program or null in case the program could not be loaded or parsed
     */
    protected Program extractProgram(File fileEntry) {
        ObjectMapper mapper = new ObjectMapper();
        Program program;

        String fileName = fileEntry.getName();
        String programName = fileName.substring(0, fileName.lastIndexOf("."));
        JsonNode programNode = null;

        try {
            if ((FilenameUtils.getExtension(fileEntry.getPath())).toLowerCase().equals("json")) {
                programNode = mapper.readTree(fileEntry);
            } else {
                programNode = JsonParser.getTargetsNodeFromJSONString(ZipReader.getJsonString(fileEntry.getPath()));
            }
        } catch (IOException e) {
            log.info("[Error] could not load program from file");
            e.printStackTrace();
        }

        if (programNode == null) {
            // TODO: Proper error handling
            log.info("[Error] project json did not contain root node");
            return null;
        }

        try {
            program = ProgramParser.parseProgram(programName, programNode);
        } catch (ParsingException | RuntimeException e) {
            // TODO: Proper error handling
            log.info("[Error] could not parse program");
            e.printStackTrace();
            return null;
        }

        return program;
    }
}