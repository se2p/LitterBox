package de.uni_passau.fim.se2.litterbox.ast.model.metadata.monitor;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class MonitorParamMetadata extends AbstractNode implements Metadata {
    private String inputName;
    private String inputValue;

    public MonitorParamMetadata(String inputName, String inputValue) {
        super();
        this.inputName = inputName;
        this.inputValue = inputValue;
    }

    public String getInputName() {
        return inputName;
    }

    public String getInputValue() {
        return inputValue;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
