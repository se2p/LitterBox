package de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ListMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.List;

public class ListMetadataList extends AbstractNode {
    private List<ListMetadata> list;

    public ListMetadataList(List<ListMetadata> list) {
        super(list);
        this.list = list;
    }

    public List<ListMetadata> getList() {
        return list;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}

