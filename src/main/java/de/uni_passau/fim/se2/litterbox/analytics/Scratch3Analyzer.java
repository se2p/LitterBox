/*
 * Copyright (C) 2019 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.utils.CSVWriter;
import de.uni_passau.fim.se2.litterbox.utils.Downloader;
import de.uni_passau.fim.se2.litterbox.utils.JsonParser;
import de.uni_passau.fim.se2.litterbox.utils.ZipReader;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import static de.uni_passau.fim.se2.litterbox.utils.GroupConstants.*;


public class Scratch3Analyzer {

    private static final Logger log = Logger.getLogger(Scratch3Analyzer.class.getName());
    private static final String SCRATCH_EXTENSION = ".sc";

    public static void analyze(String detectors, String output, File file) {
        if (file.exists() && file.isDirectory()) {
            checkMultipleScratch3(file, detectors, output);
        } else if (file.exists() && !file.isDirectory()) {
            checkSingleScratch3(file, detectors, output);
        } else {
            log.info("Folder or file '" + file.getName() + "' does not exist");
        }
    }

    /**
     * The method for analyzing one Scratch project file (ZIP). It will produce only console output.
     *
     * @param json        string of the project ot analyze the file to analyze
     * @param projectName name of the project to analyze
     * @param detectors   to be executed
     * @param csv         file where the results should be stored
     */
    public static void checkDownloaded(String json, String projectName, String detectors, String csv) {
        try {

            ObjectMapper mapper = new ObjectMapper();
            JsonNode projectNode = mapper.readTree(json);
            Program program = ProgramParser.parseProgram(projectName, projectNode);

            //System.out.println(project.toString());
            IssueTool iT = new IssueTool();
            if (csv == null || csv.equals("")) {
                iT.checkRaw(program, detectors);
            } else {
                CSVPrinter printer = prepareCSVPrinter(detectors, iT, csv);
                iT.check(program, printer, detectors);
                log.info("Finished: " + projectName);
                try {
                    assert printer != null;
                    CSVWriter.flushCSV(printer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            log.warning(e.getMessage());
        }
    }

    /**
     * The method for analyzing one Scratch project file (ZIP). It will produce only console output.
     *
     * @param fileEntry the file to analyze
     * @param detectors
     */
    private static void checkSingleScratch3(File fileEntry, String detectors, String csv) {
        Program program = extractProgram(fileEntry);

        IssueTool iT = new IssueTool();
        if (csv == null || csv.equals("")) {
            iT.checkRaw(program, detectors);
        } else {
            CSVPrinter printer = prepareCSVPrinter(detectors, iT, csv);
            iT.check(program, printer, detectors);
            System.out.println("Finished: " + fileEntry.getName());
            try {
                assert printer != null;
                CSVWriter.flushCSV(printer);
            } catch (IOException e) {
                log.warning(e.getMessage());
            }
        }
    }

    private static CSVPrinter prepareCSVPrinter(String dtctrs, IssueTool iT, String name) {
        List<String> heads = new ArrayList<>();
        heads.add("project");
        String[] detectors;
        switch (dtctrs) {
        case ALL:
            detectors = iT.getAllFinder().keySet().toArray(new String[0]);
            break;
        case BUGS:
            detectors = iT.getBugFinder().keySet().toArray(new String[0]);
            break;
        case SMELLS:
            detectors = iT.getSmellFinder().keySet().toArray(new String[0]);
            break;
        case CTSCORE:
            detectors = iT.getCTScoreFinder().keySet().toArray(new String[0]);
            break;
        default:
            detectors = dtctrs.split(",");
            break;
        }
        for (String s : detectors) {
            if (iT.getAllFinder().containsKey(s)) {
                IssueFinder iF = iT.getAllFinder().get(s);
                heads.add(iF.getName());
            }
        }
        try {
            return CSVWriter.getNewPrinter(name, heads);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * The main method for analyzing all Scratch project files (ZIP) in the given folder location. It will produce a
     * .csv file with all entries.
     */
    private static void checkMultipleScratch3(File folder, String dtctrs, String csv) {

        CSVPrinter printer = null;
        try {
            IssueTool iT = new IssueTool();
            printer = prepareCSVPrinter(dtctrs, iT, csv);
            for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
                if (!fileEntry.isDirectory()) {
                    try {
                        log.info("Start: " + fileEntry.getName());
                        Program program = extractProgram(fileEntry);
                        //System.out.println(project.toString());
                        iT.check(program, printer, dtctrs);
                        log.info("Finished: " + fileEntry.getName());
                    } catch (NullPointerException e) {
                        log.info("Ignore due to NullPointerException: " + fileEntry.getName());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert printer != null;
                CSVWriter.flushCSV(printer);
            } catch (IOException e) {
                log.warning(e.getMessage());
            }
        }
    }

    private static Program extractProgram(File fileEntry) {
        ObjectMapper mapper = new ObjectMapper();
        Program program = null;
        if ((FilenameUtils.getExtension(fileEntry.getPath())).toLowerCase().equals("json")) {
            try {
                program = ProgramParser.parseProgram(fileEntry.getName(), mapper.readTree(fileEntry));
            } catch (ParsingException | IOException e) {
                e.printStackTrace();
            }
        } else {
            JsonNode node = null;
            try {
                node = JsonParser.getTargetsNodeFromJSONString(ZipReader.getJsonString(fileEntry.getPath()));
                if (node == null) {
                    log.info("[Error] project json did not contain root node");
                }
                program = ProgramParser.parseProgram(fileEntry.getName(), node);
            } catch (ParsingException | IOException e) {
                e.printStackTrace();
            }
        }
        return program;
    }

    /**
     * Downloads and analyzes a single project with the given id
     *
     * @param projectid  Id of the project which should be downloaded
     * @param outfolder  Folder in which the project file will be stored
     * @param detectors  IssueFinders which will be run for this project
     * @param resultpath Path where the outputfile will be stored
     */
    public static void downloadAndAnalyze(String projectid, String outfolder, String detectors, String resultpath) {
        try {
            String json = Downloader.downloadAndSaveProject(projectid, outfolder);
            Scratch3Analyzer.checkDownloaded(json, projectid, //Name ProjectID is not the same as the Projectname
                    detectors, resultpath);
        } catch (IOException e) {
            log.info("Could not load project with id " + projectid);
        }
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
     * @param detectors       IssueFinders which will be run for this project
     * @param resultpath      Path where the outputfile will be stored
     */
    public static void downloadAndAnalyzeMultiple(String projectListPath,
                                                  String outfolder,
                                                  String detectors,
                                                  String resultpath) {
        File file = new File(projectListPath);

        if (!file.exists()) {
            log.info("File " + projectListPath + " does not exist.");
            return;
        } else if (file.isDirectory()) {
            log.info("File " + projectListPath + " is a directory.");
            return;
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            while (line != null) {
                line = line.trim();
                downloadAndAnalyze(line, outfolder, detectors, resultpath);
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prints the project given at {@code path} in the intermediate language.
     *
     * @param path       The path of the project.
     * @param outputFile The path to the output file.
     */
    public static void printSingleIntermediate(String path, String outputFile) {
        log.info("Starting to print " + path + " to file " + outputFile);
        File file = new File(path);
        if (!file.exists()) {
            log.info("File " + path + " does not exist.");
            return;
        } else if (file.isDirectory()) {
            log.info("File " + path + " is a directory.");
            return;
        }
        Program program = extractProgram(file);

        File output = new File(outputFile);
        try {
            boolean success = output.createNewFile();
        } catch (IOException e) {
            log.info("Could not create file at path " + outputFile);
            return;
        }
        PrintStream stream;
        try {
            stream = new PrintStream(output);
        } catch (FileNotFoundException e) {
            log.info("Creation of output stream not possible.");
            return;
        }
        GrammarPrintVisitor visitor = new GrammarPrintVisitor(stream);
        visitor.visit(program);
        stream.close();
        log.info("Finished printing.");
    }

    /**
     * Downloads the project and prints its intermediate language version.
     *
     * @param projectId   Id of the project.
     * @param projectPath The path to where the downloaded project will be stored.
     * @param printPath   The path to where the .sc file will be stored.
     */
    public static void downloadAndPrint(String projectId, String projectPath, String printPath) {
        try {
            Downloader.downloadAndSaveProject(projectId, projectPath);
        } catch (IOException e) {
            log.info("Could not load project with id " + projectId);
        }
        Path path = Paths.get(projectPath, projectId + ".json");
        printSingleIntermediate(path.toString(), printPath);
    }

    /**
     * Downloads all projects in the list and prints their intermediate language
     * version to files in the {@code projectsFolderPath}.
     *
     * @param projectListPath    The path to the list of ids.
     * @param projectsFolderPath The path to the folder in which the downloaded
     *                           projects will be stored.
     * @param printPath          The path to the folder in which the .sc files
     *                           will be stored.
     */
    public static void downloadAndPrintMultiple(String projectListPath,
                                                String projectsFolderPath,
                                                String printPath) {
        File file = new File(projectListPath);

        if (!file.exists()) {
            log.info("File " + projectListPath + " does not exist.");
            return;
        } else if (file.isDirectory()) {
            log.info("File " + projectListPath + " is a directory.");
            return;
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            while (line != null) {
                line = line.trim();
                downloadAndPrint(line, projectsFolderPath + File.separator
                        + line, printPath + File.separator + line + SCRATCH_EXTENSION);
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Prints the file or content of the folder in the intermediate language.
     *
     * @param path      The path to the file or folder to be printed.
     * @param printPath The path to the file or folder for the .sc output.
     */
    public static void printIntermediate(String path, String printPath) {
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            printMultiple(file, printPath);
        } else if (file.exists() && !file.isDirectory()) {
            printSingleIntermediate(path, printPath);
        } else {
            log.info("Folder or file '" + file.getName() + "' does not exist");
        }
    }

    private static void printMultiple(File folder, String printPath) {
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (!fileEntry.isDirectory()) {
                String name = fileEntry.getName();
                String rawName = FilenameUtils.removeExtension(name);
                printSingleIntermediate(fileEntry.getName(),
                        printPath + File.separator + rawName + SCRATCH_EXTENSION);
            }
        }
    }
}
