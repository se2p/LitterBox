package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.DotVisitor;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class DotAnalyzer extends Analyzer {
    private static final Logger log = Logger.getLogger(MetricAnalyzer.class.getName());

    public DotAnalyzer(String input, String output, boolean delete) {
        super(input, output, delete);
    }

    @Override
    void check(File fileEntry, String outputPath) {
        Program program = extractProgram(fileEntry);
        if (program == null) {
            // Todo error message
            return;
        }

        try {
            createDotFile(program, outputPath);
        } catch (IOException e) {
            log.warning("Could not create dot File: " + outputPath);
        }
    }

    private void createDotFile(Program program, String outputPath) throws IOException {
        DotVisitor graphPrinter = new DotVisitor();
        graphPrinter.initDotString();
        graphPrinter.visit(program);
        graphPrinter.finishDotString();
        graphPrinter.saveGraph(outputPath);
    }
}
