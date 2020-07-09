package de.uni_passau.fim.se2.litterbox.analytics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import de.uni_passau.fim.se2.litterbox.report.CSVReportGenerator;
import de.uni_passau.fim.se2.litterbox.report.ConsoleReportGenerator;
import de.uni_passau.fim.se2.litterbox.utils.Downloader;
import de.uni_passau.fim.se2.litterbox.utils.GroupConstants;
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
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static de.uni_passau.fim.se2.litterbox.utils.GroupConstants.*;

public class BugAnalyzer extends Analyzer {

    private static final Logger log = Logger.getLogger(BugAnalyzer.class.getName());
    private final IssueTool issueTool;
    private Set<Issue> issues;
    private String[] detectorNames;

    public BugAnalyzer(String input, String output) {
        super(input, output);
        this.issueTool = new IssueTool();
    }

    public void analyzeFile() {
        File file = input.toFile();

        if (file.exists() && file.isDirectory()) {
            for (final File fileEntry : Objects.requireNonNull(file.listFiles())) {
                if (!fileEntry.isDirectory()) {
                    check(fileEntry, String.valueOf(output));
                }
            }
        } else if (file.exists() && !file.isDirectory()) {
            check(file, String.valueOf(output));
        } else {
            log.info("Folder or file '" + file.getName() + "' does not exist");
        }
    }

    @Override
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

    @Override
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

    public void setDetectorNames(String detectorNames) {
        switch (detectorNames) {
            case ALL:
                this.detectorNames = issueTool.getAllFinder().keySet().toArray(new String[0]);
                break;
            case BUGS:
                this.detectorNames = issueTool.getBugFinder().keySet().toArray(new String[0]);
                break;
            case SMELLS:
                this.detectorNames = issueTool.getSmellFinder().keySet().toArray(new String[0]);
                break;
            default:
                this.detectorNames = detectorNames.split(",");
                break;
        }
    }

    /**
     * The method for analyzing one Scratch project file (ZIP). It will produce only console output.
     *
     * @param fileEntry the file to analyze
     */
    private void check(File fileEntry, String csv) {
        Program program = extractProgram(fileEntry);
        if (program == null) {
            // Todo error message
            return;
        }

        issues = issueTool.check(program, GroupConstants.ALL);

        // TODO: Refactor error handling
        try {
            if (csv == null || csv.isEmpty()) {
                ConsoleReportGenerator reportGenerator = new ConsoleReportGenerator(detectorNames);
                reportGenerator.generateReport(program, issues);
            } else {
                CSVReportGenerator reportGenerator = new CSVReportGenerator(csv, detectorNames);
                reportGenerator.generateReport(program, issues);
            }
        } catch (IOException e) {
            log.warning(e.getMessage());
        }
    }

    /**
     * Extracts a Scratch Program from a Json or sb3 file
     *
     * @param fileEntry of the json or sb3 file
     * @return the parsed program or null in case the program could not be loaded or parsed
     */
    private Program extractProgram(File fileEntry) {
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
