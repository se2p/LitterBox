package de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.MBlockNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.TimedStmt;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.Opcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.mBlock.RobotMoveStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.MBlockVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class TurnRightTimed extends AbstractNode implements RobotMoveStmt, TimedStmt, SinglePower {

    private final NumExpr percent;
    private final NumExpr time;
    private final BlockMetadata metadata;

    public TurnRightTimed(NumExpr percent, NumExpr time, BlockMetadata metadata) {
        super(percent, time, metadata);
        this.percent = percent;
        this.time = time;
        this.metadata = metadata;
    }

    public NumExpr getPercent() {
        return percent;
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
        return RobotMoveStmtOpcode.move_right_with_time;
    }
}
