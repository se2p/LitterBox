package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.MonitorParamMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;


/**
 * This metadata is for all monitors that do not belong to lists.
 */
public class MonitorSliderMetadata extends MonitorMetadata {
    private int sliderMin;
    private int sliderMax;
    private boolean isDiscrete;
    private String value;

    public MonitorSliderMetadata(String id, String mode, String opcode, MonitorParamMetadataList paramsMetadata,
                                 String spriteName, int width, int height, int x, int y,
                                 boolean visible, String value, int sliderMin, int sliderMax, boolean isDiscrete) {
        super(id, mode, opcode, paramsMetadata, spriteName, width, height, x, y, visible);
        this.value = value;
        this.sliderMin = sliderMin;
        this.sliderMax = sliderMax;
        this.isDiscrete = isDiscrete;
    }

    public int getSliderMin() {
        return sliderMin;
    }

    public int getSliderMax() {
        return sliderMax;
    }

    public boolean isDiscrete() {
        return isDiscrete;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
