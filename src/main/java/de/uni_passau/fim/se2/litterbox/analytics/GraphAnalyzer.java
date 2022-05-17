package de.uni_passau.fim.se2.litterbox.analytics;

import java.io.File;

import de.uni_passau.fim.se2.litterbox.analytics.ggnn.GenerateGraphTask;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

public class GraphAnalyzer extends Analyzer {
    private final String input;
    private final boolean isStageIncluded;
    private final boolean isWholeProgram;
    private final boolean isDotStringGraph;
    private final String output;
    private final String labelName;

    public GraphAnalyzer(String input, String output, boolean isStageIncluded, boolean isWholeProgram, boolean isDotStringGraph, String labelName, boolean delete) {
        super(input, output, delete);
        this.input = input;
        this.output = output;
        this.isStageIncluded = isStageIncluded;
        this.isWholeProgram = isWholeProgram;
        this.isDotStringGraph = isDotStringGraph;
        this.labelName = labelName;
    }

    @Override
    void check(File fileEntry, String outputPath) {
        Program program = extractProgram(fileEntry);
        if (program != null) {
            GenerateGraphTask generateGraphTask = new GenerateGraphTask(program, input, isStageIncluded, isWholeProgram, output, isDotStringGraph, labelName);
        } else {
            System.out.println("Program was null");
        }
    }
}
