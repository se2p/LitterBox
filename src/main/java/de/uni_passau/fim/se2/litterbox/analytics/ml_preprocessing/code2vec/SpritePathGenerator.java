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

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.StringUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ExtractSpriteVisitor;

import java.util.*;
import java.util.stream.Collectors;

public final class SpritePathGenerator extends PathGenerator {

    private final Map<ActorDefinition, List<ASTNode>> leafsMap;

    public SpritePathGenerator(Program program, int maxPathLength, boolean includeStage) {
        super(program, maxPathLength, includeStage);
        this.leafsMap = extractASTLeafs();
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
                System.out.println(i + " Leaf (Test): " + StringUtil.getToken(value));
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
        List<ProgramFeatures> spriteFeatures = new ArrayList<>();
        for (Map.Entry<ActorDefinition, List<ASTNode>> entry : leafsMap.entrySet()) {
            ActorDefinition actor = entry.getKey();
            List<ASTNode> leafs = entry.getValue();
            ProgramFeatures singleSpriteFeatures = generatePathsForSprite(actor, leafs);
            if (singleSpriteFeatures != null && !singleSpriteFeatures.isEmpty()) {
                spriteFeatures.add(singleSpriteFeatures);
            }
        }
        return spriteFeatures;
    }

    private ProgramFeatures generatePathsForSprite(final ActorDefinition sprite, final List<ASTNode> leafs) {
        String spriteName = normalizeSpriteName(sprite.getIdent().getName());
        if (spriteName == null) {
            return null;
        }
        return super.getProgramFeatures(spriteName, leafs);
    }

    @Override
    public List<String> getAllLeafs() {
        return leafsMap.values().stream().flatMap(Collection::stream).map(StringUtil::getToken)
                .collect(Collectors.toList());
    }
}
