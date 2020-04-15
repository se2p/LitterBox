package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.List;

public class ProcedurePrototypeMetadata extends BlockMetadata {
    private MutationMetadata mutation;

    public ProcedurePrototypeMetadata(CommentMetadata commentMetadata, String blockId, String opcode, String next,
                                      String parent, List<InputMetadata> inputMetadata, List<InputMetadata> fields,
                                      boolean topLevel, boolean shadow, MutationMetadata mutation) {
        super(commentMetadata, blockId, opcode, next, parent, inputMetadata, fields, topLevel, shadow);
        this.mutation = mutation;
    }

    public MutationMetadata getMutation() {
        return mutation;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

}
