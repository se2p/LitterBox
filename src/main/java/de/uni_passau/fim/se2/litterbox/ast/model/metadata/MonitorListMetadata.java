package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.MonitorParamMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.List;

public class MonitorListMetadata extends MonitorMetadata {
    private List<String> values;

    public MonitorListMetadata(String id, String mode, String opcode, MonitorParamMetadataList paramsMetadata,
                               String spriteName, int width, int height, int x, int y, boolean visible,
                               List<String> values) {
        super(id, mode, opcode, paramsMetadata, spriteName, width, height, x, y, visible);
        this.values = values;
    }

    public List<String> getValues() {
        return values;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
