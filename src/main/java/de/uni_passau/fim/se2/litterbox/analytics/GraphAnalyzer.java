package de.uni_passau.fim.se2.litterbox.analytics;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import de.uni_passau.fim.se2.litterbox.analytics.ggnn.GenerateGraphTask;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

public class GraphAnalyzer extends Analyzer {
    private static final Logger log = Logger.getLogger(GraphAnalyzer.class.getName());

    private final boolean isStageIncluded;
    private final boolean isWholeProgram;
    private final boolean isDotStringGraph;
    private final String labelName;

    public GraphAnalyzer(String input, String output, boolean isStageIncluded, boolean isWholeProgram, boolean isDotStringGraph, String labelName, boolean delete) {
        super(input, output, delete);

        this.isStageIncluded = isStageIncluded;
        this.isWholeProgram = isWholeProgram;
        this.isDotStringGraph = isDotStringGraph;
        this.labelName = labelName;
    }

    @Override
    void check(File fileEntry, String outputPath) {
        Program program = extractProgram(fileEntry);
        if (program == null) {
            log.warning("Program was null. File name was '" + fileEntry.getName() + "'");
            return;
        }

        GenerateGraphTask generateGraphTask = new GenerateGraphTask(program, input, isStageIncluded, isWholeProgram, isDotStringGraph, labelName);
        String graphData = generateGraphTask.generateGraphData();
        writeGraph(graphData);
    }

    private void writeGraph(String graphData) {
        if (graphData.isBlank()) {
            return;
        }

        if ("CONSOLE".equals(output)) {
            System.out.println(graphData);
        } else {
            writeToFile(graphData);
        }
    }

    private void writeToFile(String graphData) {
        File directory = new File(output);
        try {
            Files.createDirectories(directory.toPath());
        } catch (IOException e) {
            log.warning("Cannot create output directory.");
            return;
        }

        String format = (isDotStringGraph) ? ".dot" : ".txt";
        Path pathToFolder = Path.of(output);
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        Path pathToFile = pathToFolder.resolve("GraphData_" + timeStamp + format);

        try (var bw = Files.newBufferedWriter(pathToFile)) {
            bw.write(graphData);
        } catch (IOException e) {
            log.warning("Cannot write output to file " + pathToFile + ": " + e.getMessage());
        }
    }
}
