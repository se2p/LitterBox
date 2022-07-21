package de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.speaker;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.MBlockNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.Opcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.mBlock.SpeakerStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.MBlockVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class SetVolumeTo2 extends AbstractNode implements SpeakerStmt {
    private final NumExpr volumeValue;
    private final BlockMetadata metadata;

    public SetVolumeTo2(NumExpr volumeValue, BlockMetadata metadata) {
        super(volumeValue, metadata);
        this.volumeValue = volumeValue;
        this.metadata = metadata;
    }

    @Override
    public BlockMetadata getMetadata() {
        return metadata;
    }

    public NumExpr getVolumeValue() {
        return volumeValue;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit((MBlockNode) this);
    }

    @Override
    public void accept(MBlockVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ASTNode accept(CloneVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public Opcode getOpcode() {
        return SpeakerStmtOpcode.show_set_volume;
    }
}
