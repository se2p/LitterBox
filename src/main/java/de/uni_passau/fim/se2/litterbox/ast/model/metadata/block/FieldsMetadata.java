package de.uni_passau.fim.se2.litterbox.ast.model.metadata.block;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class FieldsMetadata extends AbstractNode implements Metadata {
    private String fieldsName;
    private String fieldsValue;
    private String fieldsReference;

    public FieldsMetadata(String fieldsName, String fieldsValue, String fieldsReference) {
        super();
        this.fieldsName = fieldsName;
        this.fieldsValue = fieldsValue;
        this.fieldsReference = fieldsReference;
    }

    public String getFieldsName() {
        return fieldsName;
    }

    public String getFieldsValue() {
        return fieldsValue;
    }

    public String getFieldsReference() {
        return fieldsReference;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
