package de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock;

import de.uni_passau.fim.se2.litterbox.ast.model.extensions.ExtensionBlock;
import de.uni_passau.fim.se2.litterbox.ast.visitor.MBlockVisitor;

public interface MBlockNode extends ExtensionBlock {

    void accept(MBlockVisitor visitor);
}
