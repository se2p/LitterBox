package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class VariableMetadata extends AbstractNode implements Metadata, ASTLeaf {
    private String variableId;
    private String variableName;
    private String value; //todo better option than string?

    public VariableMetadata(String variableId, String variableName, String value) {
        super();
        this.variableId = variableId;
        this.variableName = variableName;
        this.value = value;
    }

    public String getVariableId() {
        return variableId;
    }

    public String getVariableName() {
        return variableName;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
