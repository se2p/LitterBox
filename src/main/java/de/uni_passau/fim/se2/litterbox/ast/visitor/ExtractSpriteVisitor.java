package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;

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

            leafsCollector = traverseLeafs(node, leafsCollector, node.getIdent().getName());
            leafsMap.put(node, leafsCollector);
        }
    }

    private List<ASTNode> traverseLeafs(ASTNode node, List<ASTNode> leafsCollector, String SpriteLabel) {
        if (node instanceof ASTLeaf) {
            leafsCollector.add(node);
        }
        for (ASTNode child : node.getChildren()) {
            if (child instanceof DeclarationStmtList || child instanceof SetStmtList || child instanceof ActorType || child instanceof Metadata) {
                continue;
            }
            //Ignoring the StringLiteral which is equal to the SpriteLabel
            if (child instanceof StringLiteral && ((StringLiteral) child).getText().equals(SpriteLabel)) {
                continue;
            }
            leafsCollector = traverseLeafs(child, leafsCollector, SpriteLabel);
        }
        return leafsCollector;
    }
}
