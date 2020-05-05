package de.uni_passau.fim.se2.litterbox.ast.model.metadata.monitor;

import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.MonitorParamMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;


/**
 * This metadata is for all monitors that do not belong to lists.
 */
public class MonitorSliderMetadata extends MonitorMetadata {
    private double sliderMin;
    private double sliderMax;
    private boolean isDiscrete;
    private String value;

    public MonitorSliderMetadata(String id, String mode, String opcode, MonitorParamMetadataList paramsMetadata,
                                 String spriteName, double width, double height, double x, double y,
                                 boolean visible, String value, double sliderMin, double sliderMax,
                                 boolean isDiscrete) {
        super(id, mode, opcode, paramsMetadata, spriteName, width, height, x, y, visible);
        this.value = value;
        this.sliderMin = sliderMin;
        this.sliderMax = sliderMax;
        this.isDiscrete = isDiscrete;
    }

    public double getSliderMin() {
        return sliderMin;
    }

    public double getSliderMax() {
        return sliderMax;
    }

    public boolean isDiscrete() {
        return isDiscrete;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
