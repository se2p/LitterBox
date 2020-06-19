package de.uni_passau.fim.se2.litterbox.ast.model.metadata.input;

import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

/**
 * This class is for input that consists either of a list or a variable.
 */
public class DataInputMetadata extends InputMetadata {
    private int  dataType;
    private String dataName;
    private String dataReference;

    public DataInputMetadata(String inputName, int dataType, String dataName, String dataReference) {
        super(inputName);
        this.dataType = dataType;
        this.dataName = dataName;
        this.dataReference = dataReference;
    }

    public int getDataType() {
        return dataType;
    }

    public String getDataName() {
        return dataName;
    }

    public String getDataReference() {
        return dataReference;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
