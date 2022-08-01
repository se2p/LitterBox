package de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.speaker;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.MBlockNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.TimedStmt;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.Opcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.mBlock.SpeakerStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.MBlockVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class PlayFrequency extends AbstractNode implements SpeakerStmt, TimedStmt {

    private final BlockMetadata metadata;
    private final NumExpr frequency;
    private final NumExpr time;

    public PlayFrequency(NumExpr frequency, NumExpr time, BlockMetadata metadata) {
        super(frequency, time, metadata);
        this.frequency = frequency;
        this.time = time;
        this.metadata = metadata;
    }

    public NumExpr getFrequency() {
        return frequency;
    }

    public NumExpr getTime() {
        return time;
    }

    @Override
    public BlockMetadata getMetadata() {
        return metadata;
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
        return SpeakerStmtOpcode.show_play_hz;
    }
}
