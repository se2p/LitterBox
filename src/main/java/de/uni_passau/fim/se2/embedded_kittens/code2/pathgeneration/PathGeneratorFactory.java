/*
 * Copyright (C) 2019-2026 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.embedded_kittens.code2.pathgeneration;

import de.uni_passau.fim.se2.embedded_kittens.code2.pathgeneration.program_relation.ProgramRelationFactory;
import de.uni_passau.fim.se2.embedded_kittens.shared.ActorNameNormalizer;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

public class PathGeneratorFactory {

    private PathGeneratorFactory() {
        throw new IllegalCallerException("utility class");
    }

    public static PathGenerator createPathGenerator(
            PathType type, int maxPathLength, boolean includeStage, Program program, boolean includeDefaultSprites,
            ProgramRelationFactory programRelationFactory, ActorNameNormalizer actorNameNormalizer
    ) {
        return createPathGenerator(
            type, maxPathLength, includeStage, program, includeDefaultSprites, new PathFormatOptions(),
            programRelationFactory, actorNameNormalizer
        );
    }

    /**
     * Builds a new path generator.
     *
     * @param type                   The kind of paths that should be generated.
     * @param maxPathLength          The maximum lengths for AST paths.
     * @param includeStage           True, if the stage sprite should be included when generating paths.
     * @param program                The program for which paths should be generated.
     * @param includeDefaultSprites  True, if sprites that have default names should be included when generating paths.
     * @param pathFormatOptions      Additional options on how the path should be formatted when converting into the
     *                               output format.
     * @param programRelationFactory Defines how the paths should be represented.
     * @param actorNameNormalizer    A normalisation step that defines how the actor name should be processed.
     * @return A path generator.
     */
    public static PathGenerator createPathGenerator(
            PathType type, int maxPathLength, boolean includeStage, Program program, boolean includeDefaultSprites,
            PathFormatOptions pathFormatOptions, ProgramRelationFactory programRelationFactory,
            ActorNameNormalizer actorNameNormalizer
    ) {
        return switch (type) {
            case SCRIPT -> new ScriptEntityPathGenerator(
                    program, maxPathLength, includeStage, includeDefaultSprites, pathFormatOptions,
                    programRelationFactory
            );
            case PROGRAM -> new ProgramPathGenerator(
                    program, maxPathLength, includeStage, includeDefaultSprites, pathFormatOptions,
                    programRelationFactory
            );
            default -> new SpritePathGenerator(
                    program, maxPathLength, includeStage, includeDefaultSprites, pathFormatOptions,
                    programRelationFactory, actorNameNormalizer
            );
        };
    }
}
