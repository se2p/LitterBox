package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2vec;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;

public class PathGeneratorFactory {

    static public PathGenerator createPathGenerator(PathType type, int maxPathLength, boolean includeStage, Program program, boolean includeDefaultSprites) {

        switch (type) {
            case SCRIPT:
                return new ScriptEntityPathGenerator(program, maxPathLength, includeStage, includeDefaultSprites);
            case PROGRAM:
                return new ProgramPathGenerator(program, maxPathLength, includeStage, includeDefaultSprites);
            default:
                return new SpritePathGenerator(program, maxPathLength, includeStage, includeDefaultSprites);
        }
    }
}
