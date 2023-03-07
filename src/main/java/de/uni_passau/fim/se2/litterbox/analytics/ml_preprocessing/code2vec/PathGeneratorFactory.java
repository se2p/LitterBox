package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2vec;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;

public class PathGeneratorFactory {

    static public PathGenerator createPathGenerator(
            boolean wholeProgram, boolean isPerScript,
            int maxPathLength, boolean includeStage, Program program) {

        if (isPerScript) {
            return new ScriptEntityPathGenerator(program, maxPathLength, includeStage);
        } else if (wholeProgram) {
            return new ProgramPathGenerator(program, maxPathLength, includeStage);
        } else {
            return new SpritePathGenerator(program, maxPathLength, includeStage);
        }
    }
}
