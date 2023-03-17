package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2vec;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;

public class PathGeneratorFactory {

    static public PathGenerator createPathGenerator(PathType type, int maxPathLength, boolean includeStage, Program program) {

        switch (type) {
            case SCRIPT:
                return new ScriptEntityPathGenerator(program, maxPathLength, includeStage);
            case PROGRAM:
                return new ProgramPathGenerator(program, maxPathLength, includeStage);
            default:
                return new SpritePathGenerator(program, maxPathLength, includeStage);
        }
    }
}
