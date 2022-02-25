package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;

import java.util.*;

public class ExtractSpriteVisitor implements ScratchVisitor {

    Map<ASTNode, List<ASTNode>> leafsMap = new HashMap<>();

    public Map<ASTNode, List<ASTNode>> getLeafsCollector() {
        return leafsMap;
    }

    @Override
    public void visit(ActorDefinition node) {
        if (node.getActorType().isSprite()) {
            List<ASTNode> leafsCollector = new LinkedList<>();

            traverseLeafs(node.getScripts(), leafsCollector);
            leafsMap.put(node, leafsCollector);
        }
    }

    private void traverseLeafs(ASTNode node, List<ASTNode> leafsCollector) {
        if (node instanceof ASTLeaf) {
            leafsCollector.add(node);
        }
        for (ASTNode child : node.getChildren()) {
            //Metadata such as code position in the editor are irrelevant for the path contexts
            if (child instanceof Metadata) {
                continue;
            }

            traverseLeafs(child, leafsCollector);
        }
    }
}
