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

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.shared.TokenVisitorFactory;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.NodeNameUtil;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.StringUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ExtractSpriteVisitor;

import java.util.*;
import java.util.stream.Collectors;

public final class SpritePathGenerator extends PathGenerator {

    private final Map<ActorDefinition, List<ASTNode>> leafsMap;

    public SpritePathGenerator(Program program, int maxPathLength, boolean includeStage, boolean includeDefaultSprites) {
        super(program, maxPathLength, includeStage, includeDefaultSprites);
        this.leafsMap = Collections.unmodifiableMap(extractASTLeafs());
    }

    @Override
    public void printLeafs() {
        System.out.println("Number of sprites: " + leafsMap.keySet().size());
        for (Map.Entry<ActorDefinition, List<ASTNode>> entry : leafsMap.entrySet()) {
            String actorName = entry.getKey().getIdent().getName();
            System.out.println("Actor Definition: " + actorName);
            System.out.println("Number of ASTLeafs for " + actorName + ": " + entry.getValue().size());
            int i = 0;
            for (ASTNode value : entry.getValue()) {
                System.out.println(i + " Leaf (Test): " + TokenVisitorFactory.getNormalisedToken(value));
                i++;
            }
        }
    }

    private Map<ActorDefinition, List<ASTNode>> extractASTLeafs() {
        ExtractSpriteVisitor spriteVisitor = new ExtractSpriteVisitor(includeStage);
        program.accept(spriteVisitor);
        return spriteVisitor.getLeafsCollector();
    }

    @Override
    public List<ProgramFeatures> generatePaths() {
        final List<ProgramFeatures> spriteFeatures = new ArrayList<>();
        for (final Map.Entry<ActorDefinition, List<ASTNode>> entry : leafsMap.entrySet()) {
            final ActorDefinition actor = entry.getKey();
            final List<ASTNode> leafs = entry.getValue();
            final Optional<ProgramFeatures> singleSpriteFeatures = generatePathsForSprite(actor, leafs);
            singleSpriteFeatures.filter(features -> !features.isEmpty()).ifPresent(spriteFeatures::add);
        }
        return spriteFeatures;
    }

    private Optional<ProgramFeatures> generatePathsForSprite(final ActorDefinition sprite, final List<ASTNode> leafs) {
        final Optional<String> spriteName = NodeNameUtil.normalizeSpriteName(sprite);
        return spriteName
                .filter(name -> includeDefaultSprites || !NodeNameUtil.hasDefaultName(sprite))
                .map(name -> getProgramFeatures(name, leafs));
    }

    @Override
    public List<String> getAllLeafs() {
        return leafsMap.values().stream().flatMap(Collection::stream).map(TokenVisitorFactory::getNormalisedToken)
                .toList();
    }
}
