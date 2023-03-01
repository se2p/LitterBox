package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ExtractScriptVisitor implements ScratchVisitor{

    private final Map<Script, List<ASTNode>> leafsMap = new HashMap<>();

    @Override
    public void visit(Script node) {
            List<ASTNode> leafsCollector = new LinkedList<>();
            traverseLeafs(node.getStmtList(), leafsCollector);
            leafsMap.put(node, leafsCollector);
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

    public Map<Script, List<ASTNode>> getLeafsMap() {
        return leafsMap;
    }
}
