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

import analytics.IssueFinder;
import analytics.IssueTool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FilenameUtils;
import scratch.ast.ParsingException;
import scratch.ast.model.Program;
import scratch.ast.parser.ProgramParser;
import scratch.structure.Project;
import utils.CSVWriter;
import utils.Downloader;
import utils.JsonParser;
import utils.ZipReader;

public class Main {

    public static void main(String[] args) throws ParseException {

        Options options = new Options();

        options.addOption("path", true, "path to folder or file that should be analyzed (required)");
        options.addOption("projectid", true,
            "id of the project that should be downloaded and analysed. Only works for Scratch 3");
        options.addOption("output", true, "path with name of the csv file you want to save (required if path argument" +
            " is a folder path)");
        options.addOption("detectors", true, "name all detectors you want to run separated by ',' " +
            "\n(all detectors defined in the README)");
        options.addOption("version", true, "the Scratch Version ('2' or '3') (required)");
        CommandLineParser parser = new DefaultParser();

        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("path") && cmd.hasOption("version")) {
            File folder = new File(cmd.getOptionValue("path"));
            if (folder.exists() && folder.isDirectory()) {
                checkMultiple(folder, cmd.getOptionValue("detectors", "all"),
                    cmd.getOptionValue("output"), cmd.getOptionValue("version"));
            } else if (folder.exists() && !folder.isDirectory()) {
                checkSingle(folder, cmd.getOptionValue("detectors",
                    "all"), cmd.getOptionValue("output"), cmd.getOptionValue("version"));
            } else {
                System.err.println("[Error] folder path given does not exist");
                return;
            }
            return;
        } else if (cmd.hasOption("projectid")) {
            String projectid = cmd.getOptionValue("projectid");
            String json = null;
            try {
                json = Downloader.downloadProjectJSON(projectid);
            } catch (IOException e) {
                System.err.println("Could not load project with id " + projectid);
                return;
            }
            checkDownloaded(json, projectid, cmd.getOptionValue("detectors", //TODO projectid is not the same as name
                "all"), cmd.getOptionValue("output"));
        }
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("LitterBox", options);
        System.out.println("Example: " + "java -cp C:\\ScratchAnalytics-1.0.jar Main -path " +
            "C:\\scratchprojects\\files\\ -version 3 -output C:\\scratchprojects\\files\\test.csv -detectors cnt," +
            "glblstrt");
    }


    /**
     * The method for analyzing one Scratch project file (ZIP). It will produce only console output.
     *
     * @param json string of the project ot analyze the file to analyze
     * @param projectName name of the project to analyze
     * @param detectors to be executed
     * @param csv file where the results should be stored
     */
    private static void checkDownloaded(String json, String projectName, String detectors, String csv) {
        try {

            ObjectMapper mapper = new ObjectMapper();
            JsonNode projectNode = mapper.readTree(json);
            Program program = ProgramParser.parseProgram(projectName, projectNode);

            //System.out.println(project.toString());
            newanalytics.IssueTool it = new newanalytics.IssueTool();
            if (csv == null || csv.equals("")) {
                it.checkRaw(program, detectors);
            } else {
                //FIXME
                //     CSVPrinter printer = prepareCSVPrinter(detectors, it, csv);
                //     it.check(program, printer, detectors);
                //     System.out.println("Finished: " + fileEntry.getName());
                //     try {
                //         assert printer != null;
                //         CSVWriter.flushCSV(printer);
                //     } catch (IOException e) {
                //         e.printStackTrace();
                //     }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The method for analyzing one Scratch project file (ZIP). It will produce only console output.
     *
     * @param fileEntry the file to analyze
     * @param detectors
     */
    private static void checkSingle(File fileEntry, String detectors, String csv, String version) {
        Project project;
        try {
            if ((FilenameUtils.getExtension(fileEntry.getPath())).toLowerCase().equals("json") && version.equals(2)) {
                project = JsonParser.parseRaw(fileEntry);
            } else {
                project = getProject(fileEntry, version);
            }
            Program program = extractProgram(fileEntry);

            //System.out.println(project.toString());
            IssueTool iT = new IssueTool();
            if (csv == null || csv.equals("")) {
                iT.checkRaw(project, detectors);
            } else {
                CSVPrinter printer = prepareCSVPrinter(detectors, iT, csv);
                iT.check(project, printer, detectors);
                System.out.println("Finished: " + fileEntry.getName());
                try {
                    assert printer != null;
                    CSVWriter.flushCSV(printer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static CSVPrinter prepareCSVPrinter(String dtctrs, IssueTool iT, String name) {
        List<String> heads = new ArrayList<>();
        heads.add("project");
        String[] detectors;
        if (dtctrs.equals("all")) {
            detectors = iT.getFinder().keySet().toArray(new String[0]);
        } else {
            detectors = dtctrs.split(",");
        }
        for (String s : detectors) {
            if (iT.getFinder().containsKey(s)) {
                IssueFinder iF = iT.getFinder().get(s);
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
    private static void checkMultiple(File folder, String dtctrs, String csv, String version) {

        CSVPrinter printer = null;
        try {
            String name = csv;
            IssueTool iT = new IssueTool();
            printer = prepareCSVPrinter(dtctrs, iT, name);
            for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
                if (!fileEntry.isDirectory()) {
                    Program program = extractProgram(fileEntry);
                    Project project;
                    if ((FilenameUtils.getExtension(fileEntry.getPath())).toLowerCase().equals("json") && version
                        .equals(2)) {
                        project = JsonParser.parseRaw(fileEntry);
                    } else {
                        project = getProject(fileEntry, version);
                    }
                    //System.out.println(project.toString());
                    iT.check(project, printer, dtctrs);
                    System.out.println("Finished: " + fileEntry.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert printer != null;
                CSVWriter.flushCSV(printer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static Project getProject(File fileEntry, String version) {
        System.out.println("Starting: " + fileEntry);
        Project project = null;
        try {
            if (version.equals("2")) {
                project = JsonParser.parse2(fileEntry.getName(), fileEntry.getPath());
            } else if (version.equals("3")) {
                project = JsonParser.parse3(fileEntry.getName(), fileEntry.getPath());
            }
            assert project != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return project;
    }

    private static Program extractProgram(File fileEntry) {
        ObjectMapper mapper = new ObjectMapper();
        Program program = null;
        if ((FilenameUtils.getExtension(fileEntry.getPath())).toLowerCase().equals("json")) {
            try {
                program = ProgramParser.parseProgram(fileEntry.getName(), mapper.readTree(fileEntry));
            } catch (ParsingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            JsonNode node = null;
            try {
                node = JsonParser.getTargetsNodeFromJSONString(ZipReader.getJsonString(fileEntry.getPath()));
                if (node == null) {
                    System.err.println("[Error] project json did not contain root node");
                }
                program = ProgramParser.parseProgram(fileEntry.getName(), node);
            } catch (ParsingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return program;
    }
}
