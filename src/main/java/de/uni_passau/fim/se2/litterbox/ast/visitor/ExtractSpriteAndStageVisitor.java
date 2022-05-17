package de.uni_passau.fim.se2.litterbox.ast.visitor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorType;
import de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;

public class ExtractSpriteAndStageVisitor implements ScratchVisitor {

    Map<ASTNode, List<ASTNode>> leafsMap = new HashMap<>();

    public Map<ASTNode, List<ASTNode>> getLeafsCollector() {
        return leafsMap;
    }

    @Override
    public void visit(ActorDefinition node) {
        if (node.getActorType().isSprite() || node.getActorType().isStage()) {
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
