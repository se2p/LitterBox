package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.extensions.ExtensionBlock;

public interface ExtensionVisitor {
    void visit(ExtensionBlock node);
}
