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

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.NodeNameUtils;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.StringUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ExtractSpriteVisitor;

import java.util.*;
import java.util.stream.Collectors;

public class PathGenerator {
    private final int maxPathLength;
    private final boolean includeStage;
    private final boolean wholeProgram;

    private final Program program;
    private Map<ActorDefinition, List<ASTNode>> leafsMap;


    public PathGenerator(Program program, int maxPathLength, boolean includeStage, boolean wholeProgram) {
        this.maxPathLength = maxPathLength;
        this.includeStage = includeStage;
        this.wholeProgram = wholeProgram;
        this.program = program;

        extractASTLeafsPerSprite();
    }

    private void extractASTLeafsPerSprite() {
        ExtractSpriteVisitor spriteVisitor = new ExtractSpriteVisitor(includeStage);
        program.accept(spriteVisitor);
        leafsMap = spriteVisitor.getLeafsCollector();
    }

    public void printLeafsPerSprite() {
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

    public List<String> getAllLeafs() {
        return leafsMap.values().stream().flatMap(Collection::stream).map(StringUtil::getToken)
                .collect(Collectors.toList());
    }

    public List<ProgramFeatures> generatePaths() {
        if (wholeProgram) {
            return generatePathsWholeProgram().stream().collect(Collectors.toList());
        } else {
            return generatePathsPerSprite();
        }
    }

    private List<ProgramFeatures> generatePathsPerSprite() {
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

    private Optional<ProgramFeatures> generatePathsWholeProgram() {
        final List<ASTNode> leafs = leafsMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        final ProgramFeatures programFeatures = getProgramFeatures("program", leafs);
        return Optional.of(programFeatures).filter(features -> !features.isEmpty());
    }

    private ProgramFeatures generatePathsForSprite(final ActorDefinition sprite, final List<ASTNode> leafs) {
        String spriteName = NodeNameUtils.normalizeSpriteName(sprite.getIdent().getName());
        if (spriteName == null) {
            return null;
        }

        return getProgramFeatures(spriteName, leafs);
    }

    private ProgramFeatures getProgramFeatures(final String featureLabel, final List<ASTNode> astLeafs) {
        final ProgramFeatures programFeatures = new ProgramFeatures(featureLabel);

        for (int i = 0; i < astLeafs.size(); i++) {
            for (int j = i + 1; j < astLeafs.size(); j++) {
                ASTNode source = astLeafs.get(i);
                ASTNode target = astLeafs.get(j);
                String path = generatePath(source, target);
                if (!path.isEmpty()) {
                    String sourceLiteral = StringUtil.getToken(source);
                    String targetLiteral = StringUtil.getToken(target);
                    if (!sourceLiteral.isEmpty() && !targetLiteral.isEmpty()) {
                        programFeatures.addFeature(sourceLiteral, path, targetLiteral);
                    }
                }
            }
        }

        return programFeatures;
    }

    private static List<ASTNode> getTreeStack(ASTNode node) {
        ArrayList<ASTNode> upStack = new ArrayList<>();
        ASTNode current = node;
        while (current != null) {
            upStack.add(current);
            current = current.getParentNode();
        }
        return upStack;
    }

    private String generatePath(ASTNode source, ASTNode target) {
        String down = "_";
        String up = "^";

        final StringBuilder pathBuilder = new StringBuilder();
        final List<ASTNode> sourceStack = getTreeStack(source);
        final List<ASTNode> targetStack = getTreeStack(target);

        int commonPrefix = 0;
        int currentSourceAncestorIndex = sourceStack.size() - 1;
        int currentTargetAncestorIndex = targetStack.size() - 1;

        while (currentSourceAncestorIndex >= 0 && currentTargetAncestorIndex >= 0
                && sourceStack.get(currentSourceAncestorIndex) == targetStack.get(currentTargetAncestorIndex)) {
            commonPrefix++;
            currentSourceAncestorIndex--;
            currentTargetAncestorIndex--;
        }

        // manage too long path length
        int pathLength = sourceStack.size() + targetStack.size() - 2 * commonPrefix;
        if (maxPathLength > 0 && pathLength > maxPathLength) {
            return "";
        }

        // add source Stack Nodes until common Node(up to the common)
        for (int i = 0; i < sourceStack.size() - commonPrefix; i++) {
            ASTNode currentNode = sourceStack.get(i);
            String childId = "";
            appendNodeToPath(pathBuilder, currentNode, childId);
            pathBuilder.append(up);
        }

        // add common Node
        ASTNode commonNode = sourceStack.get(sourceStack.size() - commonPrefix);
        String commonNodeChildId = "";
        appendNodeToPath(pathBuilder, commonNode, commonNodeChildId);

        // add target Stack Nodes (down to the target)
        for (int i = targetStack.size() - commonPrefix - 1; i >= 0; i--) {
            ASTNode currentNode = targetStack.get(i);
            String childId = "";
            pathBuilder.append(down);
            appendNodeToPath(pathBuilder, currentNode, childId);
        }

        return pathBuilder.toString();
    }

    private void appendNodeToPath(final StringBuilder pathBuilder, final ASTNode node, final String childId) {
        pathBuilder.append('(').append(node.getUniqueName()).append(childId).append(')');
    }

}
