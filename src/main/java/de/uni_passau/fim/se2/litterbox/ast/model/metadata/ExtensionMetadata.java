package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.List;

public class ExtensionMetadata extends AbstractNode implements Metadata, ASTLeaf {
    private List<String> extensionNames;

    public ExtensionMetadata(List<String> extensionNames) {
        super();
        this.extensionNames = extensionNames;
    }

    public List<String> getExtensionNames() {
        return extensionNames;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
