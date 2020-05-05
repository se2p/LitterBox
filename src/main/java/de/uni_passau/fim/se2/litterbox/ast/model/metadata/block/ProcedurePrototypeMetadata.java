package de.uni_passau.fim.se2.litterbox.ast.model.metadata.block;

import de.uni_passau.fim.se2.litterbox.ast.model.metadata.CommentMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.InputMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class ProcedurePrototypeMetadata extends BlockMetadata {
    private MutationMetadata mutation;

    public ProcedurePrototypeMetadata(CommentMetadata commentMetadata, String blockId, String opcode, String next,
                                      String parent, InputMetadataList inputMetadata, InputMetadataList fields,
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
