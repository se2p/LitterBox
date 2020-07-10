package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class MetricAnalyzer extends Analyzer {

    private static final Logger log = Logger.getLogger(MetricAnalyzer.class.getName());
    private MetricTool issueTool;

    public MetricAnalyzer(String input, String output) {
        super(input, output);
        this.issueTool = new MetricTool();
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

        try {
            issueTool.createCSVFile(program, csv);
        } catch (IOException e) {
            log.warning("Could not create CSV File: " + csv);
            e.printStackTrace();
        }
    }
}
