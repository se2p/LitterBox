package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;

public class ParentVisitor implements ScratchVisitor {
    @Override
    public void visitChildren(ASTNode node) {
        for (ASTNode child : node.getChildren()) {
            child.setParentNode(node);
            child.accept(this);
        }
    }
}
