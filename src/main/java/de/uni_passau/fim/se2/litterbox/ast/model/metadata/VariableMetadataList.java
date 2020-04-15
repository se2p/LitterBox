package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.List;

public class VariableMetadataList extends AbstractNode {
    private List<VariableMetadata> list;

    public VariableMetadataList(List<VariableMetadata> list) {
        super(list);
        this.list = list;
    }

    public List<VariableMetadata> getList() {
        return list;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}