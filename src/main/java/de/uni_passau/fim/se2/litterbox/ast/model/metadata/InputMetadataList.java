package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.List;

public class InputMetadataList extends AbstractNode {
    private List<InputMetadata> list;

    public InputMetadataList(List<InputMetadata> list) {
        super(list);
        this.list = list;
    }

    public List<InputMetadata> getList() {
        return list;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
