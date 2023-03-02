package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2vec;

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.StringUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import java.util.*;

public abstract class PathGenerator {

    protected final int maxPathLength;
    protected boolean includeStage;

    protected final Program program;
    protected Map<ActorDefinition, List<ASTNode>> leafsMap;

    public PathGenerator(int maxPathLength, boolean includeStage, Program program) {
        this.maxPathLength = maxPathLength;
        this.includeStage = includeStage;
        this.program = program;
        extractASTLeafs();
    }

    public PathGenerator(int maxPathLength, Program program) {
        this.maxPathLength = maxPathLength;
        this.program = program;
        extractASTLeafs();
    }

    public abstract List<ProgramFeatures> generatePaths() ;

    public abstract void printLeafs() ;

    public abstract void extractASTLeafs() ;

    public ProgramFeatures getProgramFeatures(final String featureLabel, final List<ASTNode> astLeafs) {
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

    private static List<ASTNode> getTreeStack(ASTNode node) {
        ArrayList<ASTNode> upStack = new ArrayList<>();
        ASTNode current = node;
        while (current != null) {
            upStack.add(current);
            current = current.getParentNode();
        }
        return upStack;
    }

    private void appendNodeToPath(final StringBuilder pathBuilder, final ASTNode node, final String childId) {
        pathBuilder.append('(').append(node.getUniqueName()).append(childId).append(')');
    }

}
