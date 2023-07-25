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

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2vec.visitor.ExtractSpriteLeavesVisitor;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.shared.TokenVisitorFactory;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.NodeNameUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.util.*;

public final class SpritePathGenerator extends PathGenerator {

    private final Map<ActorDefinition, List<ASTNode>> leavesMap;

    public SpritePathGenerator(
            Program program, int maxPathLength, boolean includeStage, boolean includeDefaultSprites
    ) {
        super(program, maxPathLength, includeStage, includeDefaultSprites);
        this.leavesMap = Collections.unmodifiableMap(extractASTLeaves());
    }

    private Map<ActorDefinition, List<ASTNode>> extractASTLeaves() {
        ExtractSpriteLeavesVisitor spriteVisitor = new ExtractSpriteLeavesVisitor(
                program.getProcedureMapping(), includeStage
        );
        program.accept(spriteVisitor);
        return spriteVisitor.getLeaves();
    }

    @Override
    public List<ProgramFeatures> generatePaths() {
        final List<ProgramFeatures> spriteFeatures = new ArrayList<>();
        for (final Map.Entry<ActorDefinition, List<ASTNode>> entry : leavesMap.entrySet()) {
            final ActorDefinition actor = entry.getKey();
            final List<ASTNode> leaves = entry.getValue();
            final Optional<ProgramFeatures> singleSpriteFeatures = generatePathsForSprite(actor, leaves);
            singleSpriteFeatures.filter(features -> !features.isEmpty()).ifPresent(spriteFeatures::add);
        }
        return spriteFeatures;
    }

    private Optional<ProgramFeatures> generatePathsForSprite(final ActorDefinition sprite, final List<ASTNode> leaves) {
        final Optional<String> spriteName = NodeNameUtil.normalizeSpriteName(sprite);
        return spriteName
                .filter(name -> includeDefaultSprites || !NodeNameUtil.hasDefaultName(sprite))
                .map(name -> getProgramFeatures(name, leaves));
    }

    @Override
    public List<String> getAllLeaves() {
        return leavesMap.values()
                .stream()
                .flatMap(Collection::stream)
                .map(TokenVisitorFactory::getNormalisedToken)
                .toList();
    }
}
