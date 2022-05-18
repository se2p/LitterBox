package de.uni_passau.fim.se2.litterbox.analytics.ggnn;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.nio.file.Path;

public class GenerateGraphTask {
    private final Path inputPath;
    private final Program program;
    private final boolean isStageIncluded;
    private final boolean isWholeProgram;
    private final boolean isDotStringGraph;
    private final String labelName;

    public GenerateGraphTask(Program program, Path inputPath, boolean isStageIncluded, boolean isWholeProgram, boolean isDotStringGraph, String labelName) {
        this.inputPath = inputPath;
        this.program = program;
        this.isStageIncluded = isStageIncluded;
        this.isWholeProgram = isWholeProgram;
        this.isDotStringGraph = isDotStringGraph;
        this.labelName = labelName;
    }

    public String generateGraphData() {
        GraphGenerator graphGenerator = new GraphGenerator(program, isStageIncluded, isWholeProgram, isDotStringGraph, labelName);
        if (!isWholeProgram) {
            graphGenerator.extractGraphsPerSprite();
        }

        return graphGenerator.generateGraphs(inputPath.toString());
    }
}
