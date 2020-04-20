package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class InputMetadata extends AbstractNode implements Metadata {
    private String name;
    private int type;
    private String value;

    public InputMetadata(String name, int type, String value) {
        super();
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getName() {
        return name;
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
