package de.uni_passau.fim.se2.litterbox.ast.model.metadata.input;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public abstract class InputMetadata extends AbstractNode implements Metadata, ASTLeaf {
    private String inputName;

    public InputMetadata(String inputName) {
        super();
        this.inputName = inputName;
    }

    public String getInputName() {
        return inputName;
    }


    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
