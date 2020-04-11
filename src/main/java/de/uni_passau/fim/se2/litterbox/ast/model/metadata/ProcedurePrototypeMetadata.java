package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class ProcedurePrototypeMetadata extends BlockMetadata {
    private MutationMetadata mutation;


    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

}
