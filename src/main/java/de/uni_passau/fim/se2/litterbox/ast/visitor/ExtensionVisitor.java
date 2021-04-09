package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.ExtensionBlock;

public abstract class ExtensionVisitor {
    ScratchVisitor parent = null;

    public void addParent(ScratchVisitor scratchVisitor) {
        parent = scratchVisitor;
    }



        /**
         * Default implementation of visit method for ExtensionBlock.
         *
         * <p>
         * Iterates all children of this node without performing any action.
         * </p>
         *
         * @param node ExtensionBlock of which the children will be iterated
         */
    public void visit(ExtensionBlock node) {
        visitChildren(node);
    }

    public void visitChildren(ASTNode node) {
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
    }


}
