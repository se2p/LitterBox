package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.MonitorParamMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

/**
 * This metadata the basis for all other monitors.
 */
public abstract class MonitorMetadata extends AbstractNode implements Metadata {

    private String id;
    private String mode;
    private String opcode;
    private MonitorParamMetadataList paramsMetadata;
    private String spriteName; //TODO this can be null -> have to check when creating json from metadata
    private int width;
    private int height;
    private int x;
    private int y;
    private boolean visible;

    public MonitorMetadata(String id, String mode, String opcode, MonitorParamMetadataList paramsMetadata,
                           String spriteName, int width, int height, int x, int y, boolean visible) {
        super(paramsMetadata);
        this.id = id;
        this.mode = mode;
        this.opcode = opcode;
        this.paramsMetadata = paramsMetadata;
        this.spriteName = spriteName;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.visible = visible;
    }

    public String getId() {
        return id;
    }

    public String getMode() {
        return mode;
    }

    public String getOpcode() {
        return opcode;
    }

    public MonitorParamMetadataList getParamsMetadata() {
        return paramsMetadata;
    }

    public String getSpriteName() {
        return spriteName;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isVisible() {
        return visible;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
