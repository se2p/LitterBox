package de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.BroadcastMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.MonitorParamMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.List;

public class MonitorParamMetadataList extends AbstractNode {
    private List<MonitorParamMetadata> list;

    public MonitorParamMetadataList(List<MonitorParamMetadata> list) {
        super(list);
        this.list = list;
    }

    public List<MonitorParamMetadata> getList() {
        return list;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

}
