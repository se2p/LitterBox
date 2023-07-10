/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2vec;

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2vec.visitor.ExtractSpriteVisitor;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.shared.TokenVisitorFactory;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.util.*;

public final class ProgramPathGenerator extends PathGenerator {

    private final Map<ActorDefinition, List<ASTNode>> leafsMap;

    public ProgramPathGenerator(
            Program program, int maxPathLength, boolean includeStage, boolean includeDefaultSprites
    ) {
        super(program, maxPathLength, includeStage, includeDefaultSprites);
        this.leafsMap = Collections.unmodifiableMap(extractASTLeafs());
    }

    private Map<ActorDefinition, List<ASTNode>> extractASTLeafs() {
        ExtractSpriteVisitor spriteVisitor = new ExtractSpriteVisitor(includeStage);
        program.accept(spriteVisitor);
        return spriteVisitor.getLeafsCollector();
    }

    @Override
    public List<ProgramFeatures> generatePaths() {
        return generatePathsWholeProgram().stream().toList();
    }

    private Optional<ProgramFeatures> generatePathsWholeProgram() {
        List<ASTNode> leafs = leafsMap.values().stream().flatMap(Collection::stream).toList();
        final ProgramFeatures programFeatures = super.getProgramFeatures("program", leafs);
        return Optional.of(programFeatures).filter(features -> !features.isEmpty());
    }

    @Override
    public List<String> getAllLeafs() {
        return leafsMap.values()
                .stream()
                .flatMap(Collection::stream)
                .map(TokenVisitorFactory::getNormalisedToken)
                .toList();
    }
}
