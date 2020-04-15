package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.List;

public class MonitorMetadata extends AbstractNode implements Metadata {

    private String id;
    private String mode;
    private String opcode;
    private List<ParamMetadata> paramsMetadata;
    private String spriteName;
    private String value;
    private int width;
    private int height;
    private int x;
    private int y;
    private boolean visible;
    private int sliderMin; // only exists if it the monitor does not belong to a list
    private int sliderMax; // only exists if it the monitor does not belong to a list
    private boolean isDiscrete; // only exists if it the monitor does not belong to a list

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
