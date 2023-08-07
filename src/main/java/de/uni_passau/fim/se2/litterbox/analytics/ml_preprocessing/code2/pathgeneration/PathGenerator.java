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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2.pathgeneration;

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2.pathgeneration.program_relation.ProgramRelationFactory;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.shared.TokenVisitorFactory;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.StringUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public abstract class PathGenerator {

    private static final PathRepresentationVisitor PATH_REPRESENTATION_VISITOR = new PathRepresentationVisitor();

    protected final Program program;

    protected final int maxPathLength;
    protected final boolean includeStage;
    protected final boolean includeDefaultSprites;

    private final PathFormatOptions pathFormatOptions;
    private final ProgramRelationFactory programRelationFactory;

    protected PathGenerator(
            Program program, int maxPathLength, boolean includeStage, boolean includeDefaultSprites,
            PathFormatOptions pathFormatOptions, ProgramRelationFactory programRelationFactory
    ) {
        this.maxPathLength = maxPathLength;
        this.includeStage = includeStage;
        this.program = program;
        this.includeDefaultSprites = includeDefaultSprites;
        this.pathFormatOptions = pathFormatOptions;
        this.programRelationFactory = programRelationFactory;
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

    private void appendNodeToPath(final StringBuilder pathBuilder, final ASTNode node) {
        pathBuilder.append(pathFormatOptions.startSymbol());
        if (pathFormatOptions.useNodeId()) {
            pathBuilder.append(getNodeTypeId(node));
        } else {
            pathBuilder.append(node.getUniqueName());
        }
        pathBuilder.append(pathFormatOptions.endSymbol());
    }

    private int getNodeTypeId(final ASTNode node) {
        node.accept(PATH_REPRESENTATION_VISITOR);
        return PATH_REPRESENTATION_VISITOR.getRepresentation();
    }

    public abstract List<ProgramFeatures> generatePaths();

    public List<String> getAllLeaves() {
        return getLeaves().map(TokenVisitorFactory::getNormalisedToken).toList();
    }

    protected abstract Stream<ASTNode> getLeaves();

    protected final ProgramFeatures getProgramFeatures(final String featureLabel, final List<ASTNode> astLeaves) {
        final ProgramFeatures programFeatures = new ProgramFeatures(featureLabel, programRelationFactory);

        for (int i = 0; i < astLeaves.size(); i++) {
            for (int j = i + 1; j < astLeaves.size(); j++) {
                final ASTNode source = astLeaves.get(i);
                final ASTNode target = astLeaves.get(j);
                final String path = generatePath(source, target);

                if (path != null) {
                    addFeature(programFeatures, source, target, path);
                }
            }
        }

        return programFeatures;
    }

    private void addFeature(
            final ProgramFeatures programFeatures, final ASTNode source, final ASTNode target, final String path
    ) {
        String sourceLiteral = TokenVisitorFactory.getNormalisedTokenWithDelimiter(
                source, pathFormatOptions.delimiter()
        );
        String targetLiteral = TokenVisitorFactory.getNormalisedTokenWithDelimiter(
                target, pathFormatOptions.delimiter()
        );

        if (pathFormatOptions.normaliseTokens()) {
            sourceLiteral = StringUtil.normaliseString(sourceLiteral, pathFormatOptions.delimiter());
            targetLiteral = StringUtil.normaliseString(sourceLiteral, pathFormatOptions.delimiter());
        }

        if (!sourceLiteral.isEmpty() && !targetLiteral.isEmpty()) {
            programFeatures.addFeature(sourceLiteral, path, targetLiteral);
        }
    }

    private String generatePath(ASTNode source, ASTNode target) {
        final List<ASTNode> sourceStack = getTreeStack(source);
        final List<ASTNode> targetStack = getTreeStack(target);

        int commonPrefix = 0;
        int currentSourceAncestorIndex = sourceStack.size() - 1;
        int currentTargetAncestorIndex = targetStack.size() - 1;

        while (
                currentSourceAncestorIndex >= 0
                        && currentTargetAncestorIndex >= 0
                        && sourceStack.get(currentSourceAncestorIndex) == targetStack.get(currentTargetAncestorIndex)
        ) {
            commonPrefix++;
            currentSourceAncestorIndex--;
            currentTargetAncestorIndex--;
        }

        // manage too long path length
        int pathLength = sourceStack.size() + targetStack.size() - 2 * commonPrefix;
        if (maxPathLength > 0 && pathLength > maxPathLength) {
            return null;
        }

        return buildPath(sourceStack, targetStack, commonPrefix);
    }

    private String buildPath(final List<ASTNode> sourceStack, final List<ASTNode> targetStack, final int commonPrefix) {
        final StringBuilder pathBuilder = new StringBuilder();

        addSourceStackToPath(pathBuilder, sourceStack, commonPrefix);
        final ASTNode commonNode = sourceStack.get(sourceStack.size() - commonPrefix);
        appendNodeToPath(pathBuilder, commonNode);
        addTargetStackToPath(pathBuilder, targetStack, commonPrefix);

        return pathBuilder.toString();
    }

    private void addSourceStackToPath(
            final StringBuilder pathBuilder, final List<ASTNode> sourceStack, final int commonPrefix
    ) {
        for (int i = 0; i < sourceStack.size() - commonPrefix; i++) {
            ASTNode currentNode = sourceStack.get(i);
            appendNodeToPath(pathBuilder, currentNode);
            pathBuilder.append(pathFormatOptions.upSymbol());
        }
    }

    private void addTargetStackToPath(
            final StringBuilder pathBuilder, final List<ASTNode> targetStack, final int commonPrefix
    ) {
        for (int i = targetStack.size() - commonPrefix - 1; i >= 0; i--) {
            ASTNode currentNode = targetStack.get(i);
            pathBuilder.append(pathFormatOptions.downSymbol());
            appendNodeToPath(pathBuilder, currentNode);
        }
    }
}
