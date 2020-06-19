package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.MonitorMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class ProgramMetadata extends AbstractNode implements Metadata {
    private MetaMetadata meta;
    private MonitorMetadataList monitor;
    private ExtensionMetadata extension;

    public ProgramMetadata(MonitorMetadataList monitor, ExtensionMetadata extension, MetaMetadata meta) {
        super(monitor, extension, meta);
        this.meta = meta;
        this.monitor = monitor;
        this.extension = extension;
    }

    public MetaMetadata getMeta() {
        return meta;
    }

    public MonitorMetadataList getMonitor() {
        return monitor;
    }

    public ExtensionMetadata getExtension() {
        return extension;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
