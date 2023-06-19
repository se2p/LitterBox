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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.pathgeneration;

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.pathgeneration.ProgramFeatures;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.shared.TokenVisitorFactory;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.NodeNameUtil;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.StringUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ExtractSpriteVisitor;

import java.util.*;

public class PathGenerator {
    private final int maxPathLength;
    private final boolean includeStage;
    private final boolean wholeProgram;
    private final boolean includeDefaultSprites;
    private final String delimiter;
    private final String upSymbol;
    private final String downSymbol;
    private final String startSymbol;
    private final String endSymbol;
    private final boolean normaliseTokens;

    private final Program program;
    private Map<ActorDefinition, List<ASTNode>> leafsMap;

    public PathGenerator(Program program, int maxPathLength, boolean includeStage, boolean wholeProgram,
                         boolean includeDefaultSprites) {
        this.maxPathLength = maxPathLength;
        this.includeStage = includeStage;
        this.wholeProgram = wholeProgram;
        this.program = program;
        this.includeDefaultSprites = includeDefaultSprites;
        this.delimiter = "_";
        this.upSymbol = "^";
        this.downSymbol = "_";
        this.startSymbol = "(";
        this.endSymbol = ")";
        this.normaliseTokens = false;

        extractASTLeafsPerSprite();
    }

    public PathGenerator(Program program, int maxPathLength, boolean includeStage, boolean wholeProgram,
                         boolean includeDefaultSprites, String delimiter, String upSymbol, String downSymbol,
                         String startSymbol, String endsymbol, boolean normaliseTokens) {
        this.maxPathLength = maxPathLength;
        this.includeStage = includeStage;
        this.wholeProgram = wholeProgram;
        this.program = program;
        this.includeDefaultSprites = includeDefaultSprites;
        this.delimiter = delimiter;
        this.upSymbol = upSymbol;
        this.downSymbol = downSymbol;
        this.startSymbol = startSymbol;
        this.endSymbol = endsymbol;
        this.normaliseTokens = normaliseTokens;

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
                System.out.println(i + " Leaf (Test): " + TokenVisitorFactory.getNormalisedToken(value));
                i++;
            }
        }
    }

    public List<String> getAllLeafs() {
        return leafsMap.values()
                .stream()
                .flatMap(Collection::stream)
                .map(TokenVisitorFactory::getNormalisedToken)
                .toList();
    }

    public List<ProgramFeatures> generatePaths() {
        if (wholeProgram) {
            return generatePathsWholeProgram().stream().toList();
        } else {
            return generatePathsPerSprite();
        }
    }

    private List<ProgramFeatures> generatePathsPerSprite() {
        final List<ProgramFeatures> spriteFeatures = new ArrayList<>();

        for (final Map.Entry<ActorDefinition, List<ASTNode>> entry : leafsMap.entrySet()) {
            final ActorDefinition actor = entry.getKey();
            final List<ASTNode> leafs = entry.getValue();

            final Optional<ProgramFeatures> singleSpriteFeatures = generatePathsForSprite(actor, leafs);
            singleSpriteFeatures.filter(features -> !features.isEmpty()).ifPresent(spriteFeatures::add);
        }

        return spriteFeatures;
    }

    private Optional<ProgramFeatures> generatePathsWholeProgram() {
        final List<ASTNode> leafs = leafsMap.values().stream().flatMap(Collection::stream).toList();
        final ProgramFeatures programFeatures = getProgramFeatures("program", leafs);
        return Optional.of(programFeatures).filter(features -> !features.isEmpty());
    }

    private Optional<ProgramFeatures> generatePathsForSprite(final ActorDefinition sprite, final List<ASTNode> leafs) {
        final Optional<String> spriteName = NodeNameUtil.normalizeSpriteName(sprite);
        return spriteName
                .filter(name -> includeDefaultSprites || !NodeNameUtil.hasDefaultName(sprite))
                .map(name -> getProgramFeatures(name, leafs));
    }

    private ProgramFeatures getProgramFeatures(final String featureLabel, final List<ASTNode> astLeafs) {
        final ProgramFeatures programFeatures = new ProgramFeatures(featureLabel);

        for (int i = 0; i < astLeafs.size(); i++) {
            for (int j = i + 1; j < astLeafs.size(); j++) {
                ASTNode source = astLeafs.get(i);
                ASTNode target = astLeafs.get(j);
                String path = generatePath(source, target);
                if (!path.isEmpty()) {
                    String sourceLiteral = TokenVisitorFactory.getNormalisedTokenWithDelimiter(source, delimiter);
                    String targetLiteral = TokenVisitorFactory.getNormalisedTokenWithDelimiter(target, delimiter);
                    if (normaliseTokens) {
                        sourceLiteral = StringUtil.normaliseString(sourceLiteral, delimiter);
                        targetLiteral = StringUtil.normaliseString(targetLiteral, delimiter);
                    }
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
            pathBuilder.append(upSymbol);
        }

        // add common Node
        ASTNode commonNode = sourceStack.get(sourceStack.size() - commonPrefix);
        String commonNodeChildId = "";
        appendNodeToPath(pathBuilder, commonNode, commonNodeChildId);

        // add target Stack Nodes (down to the target)
        for (int i = targetStack.size() - commonPrefix - 1; i >= 0; i--) {
            ASTNode currentNode = targetStack.get(i);
            String childId = "";
            pathBuilder.append(downSymbol);
            appendNodeToPath(pathBuilder, currentNode, childId);
        }

        return pathBuilder.toString();
    }

    private void appendNodeToPath(final StringBuilder pathBuilder, final ASTNode node, final String childId) {
        pathBuilder.append(startSymbol).append(node.getUniqueName()).append(childId).append(endSymbol);
    }
}
