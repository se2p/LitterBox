package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.analytics.code2vec.GeneratePathTask;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.LeilaVisitor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class Code2VecAnalyzer extends Analyzer{
    private static final Logger log = Logger.getLogger(Code2VecAnalyzer.class.getName());
    private final int maxPathLength;

    public Code2VecAnalyzer(String input, String output, int maxPathLength, boolean delete) {
        super(input, output, delete);
        this.maxPathLength = maxPathLength;
    }

    @Override
    void check(File fileEntry, String outputPath) {
        Program program = extractProgram(fileEntry);
        if(program == null) {
            log.warning("Program was null. File name was '" + fileEntry.getName() + "'");
            return;
        }

        GeneratePathTask generatePathTask = new GeneratePathTask(program, maxPathLength);
        String code2VecInput = generatePathTask.createContextForCode2Vec();

        if (code2VecInput.length() > 0) {
            if (outputPath.equals("CONSOLE")) {
                System.out.println(code2VecInput);
            } else {
                writeToFile(fileEntry.getName(), outputPath, code2VecInput);
            }
        }
    }

    private void writeToFile(String fileName, String outputPath, String code2VecInput) {
        if (!Paths.get(outputPath).toFile().isDirectory()) {
            log.warning("Output path must be a folder");
            return;
        }

        PrintStream stream;
        String outName = fileName.substring(0, fileName.lastIndexOf("."));

        try {
            Path outPath = Paths.get(outputPath, outName);
            stream = new PrintStream(outPath.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.info("Creation of output stream not possible with output file " + outName);
            return;
        }
        log.info("Starting to print " + fileName + " to file " + outputPath);
        stream.print(code2VecInput);
        stream.close();
        log.info("Finished printing.");
    }

}
