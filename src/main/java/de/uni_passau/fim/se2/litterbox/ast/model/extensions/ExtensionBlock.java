package de.uni_passau.fim.se2.litterbox.ast.model.extensions;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ExtensionVisitor;

public interface ExtensionBlock extends ASTNode {

    void accept(ExtensionVisitor visitor);
}
