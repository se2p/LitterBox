package de.uni_passau.fim.se2.litterbox.ast.model.metadata.input;

import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

/**
 * This input consists of directly entered literals
 */
public class TypeInputMetadata extends InputMetadata {
    private int type;
    private String value;

    public TypeInputMetadata(String inputName, int type, String value) {
        super(inputName);
        this.type = type;
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
