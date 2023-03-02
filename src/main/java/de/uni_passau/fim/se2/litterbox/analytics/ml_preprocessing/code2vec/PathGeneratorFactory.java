package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2vec;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;

public class PathGeneratorFactory {

    static public PathGenerator createPathGenerator(
            boolean wholeProgram, boolean isPerScript,
            int maxPathLength, boolean includeStage, Program program) {

        if (isPerScript) {
            return new ScriptPathGenerator(maxPathLength, program);
        } else if (wholeProgram) {
            return new ProgramPathGenerator(maxPathLength, includeStage, program);
        } else {
            return new SpritePathGenerator(maxPathLength, includeStage, program);
        }
    }
}
