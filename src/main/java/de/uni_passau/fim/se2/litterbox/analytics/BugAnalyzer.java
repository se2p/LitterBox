package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.jsonCreation.JSONFileCreator;
import de.uni_passau.fim.se2.litterbox.report.CSVReportGenerator;
import de.uni_passau.fim.se2.litterbox.report.CommentGenerator;
import de.uni_passau.fim.se2.litterbox.report.ConsoleReportGenerator;
import de.uni_passau.fim.se2.litterbox.utils.GroupConstants;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;

import static de.uni_passau.fim.se2.litterbox.utils.GroupConstants.*;

public class BugAnalyzer extends Analyzer {

    private static final Logger log = Logger.getLogger(BugAnalyzer.class.getName());
    private final IssueTool issueTool;
    private String[] detectorNames;
    private String annotationOutput;

    public BugAnalyzer(String input, String output) {
        super(input, output);
        this.issueTool = new IssueTool();
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

    public void setAnnotationOutput(String annotationOutput) {
        this.annotationOutput = annotationOutput;
    }

    /**
     * The method for analyzing one Scratch project file (ZIP). It will produce only console output.
     *
     * @param fileEntry the file to analyze
     */
    void check(File fileEntry, String csv) {
        Program program = extractProgram(fileEntry);
        if (program == null) {
            // Todo error message
            return;
        }

        Set<Issue> issues = issueTool.check(program, GroupConstants.ALL);

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

        if (annotationOutput != null && !annotationOutput.isEmpty()) {
            try {
                CommentGenerator commentGenerator = new CommentGenerator();
                commentGenerator.generateReport(program, issues);
                createAnnotatedFile(fileEntry, program, annotationOutput);
            } catch (IOException e) {
                log.warning(e.getMessage());
            }
        }
    }

    private void createAnnotatedFile(File fileEntry, Program program, String annotatePath) throws IOException {
        if ((FilenameUtils.getExtension(fileEntry.getPath())).toLowerCase().equals("json")) {
            JSONFileCreator.writeJsonFromProgram(program, annotatePath);
        } else {
            JSONFileCreator.writeSb3FromProgram(program, annotatePath, fileEntry);
        }
    }
}


