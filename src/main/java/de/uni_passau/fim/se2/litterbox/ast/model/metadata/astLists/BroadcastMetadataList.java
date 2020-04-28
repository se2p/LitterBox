package de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.BroadcastMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.List;

public class BroadcastMetadataList extends AbstractNode {
    private List<BroadcastMetadata> list;

    public BroadcastMetadataList(List<BroadcastMetadata> list) {
        super(list);
        this.list = list;
    }

    public List<BroadcastMetadata> getList() {
        return list;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
