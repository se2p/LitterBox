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
import de.uni_passau.fim.se2.litterbox.ast.visitor.GrammarPrintVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Downloader;
import de.uni_passau.fim.se2.litterbox.utils.JsonParser;
import de.uni_passau.fim.se2.litterbox.utils.ZipReader;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Logger;

import static org.apache.commons.io.FilenameUtils.removeExtension;

public class Scratch3Analyzer {

    private static final Logger log = Logger.getLogger(Scratch3Analyzer.class.getName());
    private static final String INTERMEDIATE_EXTENSION = ".sc";

    public static void statsProject(String output, File file) throws IOException {
        if (file.exists() && file.isDirectory()) {
            statsMultipleScratch3(file, output);
        } else if (file.exists() && !file.isDirectory()) {
            statsSingleScratch3(file, output);
        } else {
            log.info("Folder or file '" + file.getName() + "' does not exist");
        }
    }

    private static void statsSingleScratch3(File fileEntry, String csv) throws IOException {
        Program program = extractProgram(fileEntry);
        MetricAnalyzer analyzer = new MetricAnalyzer();
        analyzer.createCSVFile(program, csv);
    }

    private static void statsMultipleScratch3(File folder, String csv) throws IOException {

        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (!fileEntry.isDirectory()) {
                log.info("Start: " + fileEntry.getName());
                Program program = extractProgram(fileEntry);
                MetricAnalyzer analyzer = new MetricAnalyzer();
                analyzer.createCSVFile(program, csv);
                log.info("Finished: " + fileEntry.getName());
            }
        }
    }

    public static void statsDownloaded(String json, String projectName, String csv) throws ParsingException, IOException {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode projectNode = mapper.readTree(json);
        Program program = ProgramParser.parseProgram(projectName, projectNode);

        MetricAnalyzer analyzer = new MetricAnalyzer();
        analyzer.createCSVFile(program, csv);
    }


    private static Program extractProgram(File fileEntry) {
        ObjectMapper mapper = new ObjectMapper();
        Program program = null;
        if ((FilenameUtils.getExtension(fileEntry.getPath())).toLowerCase().equals("json")) {
            try {
                program = ProgramParser.parseProgram(fileEntry.getName().substring(0, fileEntry.getName().lastIndexOf(
                        ".") - 1),
                        mapper.readTree(fileEntry));
            } catch (ParsingException | IOException | RuntimeException e) {
                // TODO: Proper error handling
                e.printStackTrace();
            }
        } else {
            JsonNode node;
            try {
                node = JsonParser.getTargetsNodeFromJSONString(ZipReader.getJsonString(fileEntry.getPath()));
                if (node == null) {
                    // TODO: Proper error handling
                    log.info("[Error] project json did not contain root node");
                    return null;
                }
                program = ProgramParser.parseProgram(fileEntry.getName().substring(0, fileEntry.getName().lastIndexOf(
                        ".")), node);
            } catch (ParsingException | IOException | RuntimeException e) {
                // TODO: Proper error handling
                e.printStackTrace();
            }
        }
        return program;
    }


    public static void downloadAndStats(String projectid, String outfolder, String resultpath) throws IOException, ParsingException {
        String json = Downloader.downloadAndSaveProject(projectid, outfolder);
        Scratch3Analyzer.statsDownloaded(json, projectid, resultpath);
    }

    /**
     * Downloads all projects with the ids in a file at the given path.
     *
     * <p>
     * The file at the given path is expected to contain a list of project ids.
     * The projects are then downloaded, stored and analyzed.
     *
     * @param projectListPath Path to the file with project ids.
     * @param outfolder       Folder in which the project file will be stored
     * @param resultpath      Path where the outputfile will be stored
     */
    public static void downloadAndStatsMultiple(String projectListPath,
                                                  String outfolder,
                                                  String resultpath) throws IOException, ParsingException {
        File file = new File(projectListPath);
        BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
        String line = br.readLine();
        while (line != null) {
            line = line.trim();
            downloadAndStats(line, outfolder, resultpath);
            line = br.readLine();
        }
        br.close();
    }
    /**
     * Prints the project given at {@code path} in the intermediate language.
     *
     * @param path           The path of the project.
     * @param outputFilePath The path to the output file.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void printSingleIntermediate(String path, String outputFilePath) {
        File file = new File(path);
        if (!file.exists()) {
            log.info("File " + path + " does not exist.");
        } else if (file.isDirectory()) {
            log.info("File " + path + " is a directory.");
        } else {
            File outputFile = new File(outputFilePath);
            if (outputFile.isDirectory() && !outputFile.exists()) {
                log.info("The path " + outputFilePath + " does not exist."
                        + "Please enter an existing path.");
            } else {
                if (outputFile.isDirectory()) {
                    outputFilePath = removeEndSeparator(outputFilePath) + File.separator +
                            removeExtension(file.getName()) + INTERMEDIATE_EXTENSION;
                    outputFile = new File(outputFilePath);
                }
                try {
                    outputFile.createNewFile();
                } catch (IOException e) {
                    log.info("Creating file " + outputFilePath + " failed. "
                            + "Please make sure all the directories in the path exist.");
                    return;
                }
            }

            PrintStream stream;
            try {
                stream = new PrintStream(outputFile, StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.info("Creation of output stream not possible with output file " + outputFilePath);
                return;
            }
            log.info("Starting to print " + path + " to file " + outputFilePath);
            GrammarPrintVisitor visitor = new GrammarPrintVisitor(stream);
            Program program = extractProgram(file);
            visitor.visit(program);
            stream.close();
            log.info("Finished printing.");
        }
    }

    /**
     * Downloads the project and prints its intermediate language version.
     *
     * @param projectId   Id of the project.
     * @param projectPath The path to where the downloaded project will be stored.
     * @param printPath   The path to where the .sc file will be stored.
     */
    public static void downloadAndPrint(String projectId, String projectPath, String printPath) throws IOException {
        Downloader.downloadAndSaveProject(projectId, projectPath);
        Path path = Paths.get(projectPath, projectId + ".json");
        printSingleIntermediate(path.toString(), printPath);
    }

    /**
     * Downloads all projects in the list and prints their intermediate language
     * version to files in the {@code projectPath}.
     *
     * @param projectListPath The path to the list of ids.
     * @param projectPath     The path to the folder in which the downloaded
     *                        projects will be stored.
     * @param printPath       The path to the folder in which the .sc files
     *                        will be stored.
     */
    public static void downloadAndPrintMultiple(String projectListPath,
                                                String projectPath,
                                                String printPath) throws IOException {
        File file = new File(projectListPath);

        // TODO: Inconsistent error handling
        if (!file.exists()) {
            log.info("File " + projectListPath + " does not exist.");
            return;
        } else if (file.isDirectory()) {
            log.info("File " + projectListPath + " is a directory.");
            return;
        }

        BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
        String line = br.readLine();
        while (line != null) {
            line = line.trim();
            downloadAndPrint(line, projectPath, printPath + File.separator + line + INTERMEDIATE_EXTENSION);
            line = br.readLine();
        }
        br.close();
    }

    /**
     * Prints the file or content of the folder in the intermediate language.
     *
     * @param projectPath The projectPath to the file or folder to be printed.
     * @param printPath   The projectPath to the file or folder for the .sc output.
     */
    public static void printIntermediate(String projectPath, String printPath) {
        File file = new File(projectPath);
        if (file.exists() && file.isDirectory()) {
            printMultiple(file, removeEndSeparator(printPath));
        } else if (file.exists() && !file.isDirectory()) {
            printSingleIntermediate(projectPath, printPath);
        } else {
            log.info("Folder or file '" + file.getName() + "' does not exist");
        }
    }

    /**
     * Prints every project in the {@code folder} to a separate file in the
     * {@code printPath}.
     *
     * @param folder    The folder containing scratch projects.
     * @param printPath The directory to save the .sc files to (without end separator).
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void printMultiple(File folder, String printPath) {
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (!fileEntry.isDirectory()) {
                String name = fileEntry.getName();
                String rawName = removeExtension(name);
                String outputFilePath = printPath + File.separator + rawName + INTERMEDIATE_EXTENSION;
                File outputFile = new File(outputFilePath);
                try {
                    outputFile.createNewFile();
                } catch (IOException e) {
                    log.info("Creating a file at " + outputFilePath + " failed.");
                    continue;
                }
                printSingleIntermediate(fileEntry.getPath(),
                        outputFilePath);
            }
        }
    }

    /**
     * Removes the end separator of the path if present.
     *
     * @param path The path.
     * @return The path without its end separator.
     */
    public static String removeEndSeparator(String path) {
        if (path == null) {
            return null;
        } else if (path.endsWith("/") || path.endsWith("\\")) {
            return path.substring(0, path.length() - 1);
        } else {
            return path;
        }
    }
}
