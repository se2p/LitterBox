package de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.monitor.MonitorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.List;

public class MonitorMetadataList extends AbstractNode {
    private List<MonitorMetadata> list;

    public MonitorMetadataList(List<MonitorMetadata> list) {
        super(list);
        this.list = list;
    }

    public List<MonitorMetadata> getList() {
        return list;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}