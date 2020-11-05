package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.analytics.code2vec.GeneratePathTask;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.io.File;

public class Code2VecAnalyzer extends Analyzer{
    private final int maxPathLength;
    private final int maxPathWidth;

    public Code2VecAnalyzer(String input, String output, int maxPathLength, int maxPathWidth, boolean delete) {
        super(input, output, delete);
        this.maxPathLength = maxPathLength;
        this.maxPathWidth = maxPathWidth;
    }

    @Override
    void check(File fileEntry, String outputPath) {
        Program program = extractProgram(fileEntry);
        if(program != null) {
            GeneratePathTask generatePathTask = new GeneratePathTask(program, maxPathLength, maxPathWidth);
        } else {
            System.out.println("Program was null");
        }
    }
}
