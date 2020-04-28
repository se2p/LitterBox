package de.uni_passau.fim.se2.litterbox.ast.model.metadata.input;

import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;


/**
 * This input consists of other blocks used as an input including parameters.
 */
public class ReferenceInputMetadata extends InputMetadata {
    private String reference;

    public ReferenceInputMetadata(String inputName, String reference) {
        super(inputName);
        this.reference = reference;
    }

    public String getReference() {
        return reference;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

}
