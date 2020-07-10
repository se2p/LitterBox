package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.GrammarPrintVisitor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class PrintAnalyzer extends Analyzer {

    private static final String INTERMEDIATE_EXTENSION = ".sc";
    private static final Logger log = Logger.getLogger(MetricAnalyzer.class.getName());

    public PrintAnalyzer(String input, String output) {
        super(input, output);
    }

    @Override
    void check(File fileEntry, String out) {
        if (!Paths.get(out).toFile().isDirectory()) {
            log.warning("Output path must be a folder");
            return;
        }

        PrintStream stream;
        String outName = getIntermediateFileName(fileEntry.getName());

        try {
            Path outPath = Paths.get(out, outName);
            stream = new PrintStream(outPath.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.info("Creation of output stream not possible with output file " + outName);
            return;
        }
        log.info("Starting to print " + fileEntry.getName() + " to file " + out);
        GrammarPrintVisitor visitor = new GrammarPrintVisitor(stream);
        Program program = extractProgram(fileEntry);
        visitor.visit(program);
        stream.close();
        log.info("Finished printing.");
    }

    private String getIntermediateFileName(String name) {
        String programName = name.substring(0, name.lastIndexOf("."));
        StringBuilder builder = new StringBuilder();
        builder.append(programName);
        builder.append(".");
        builder.append(INTERMEDIATE_EXTENSION);
        return builder.toString();
    }
}
