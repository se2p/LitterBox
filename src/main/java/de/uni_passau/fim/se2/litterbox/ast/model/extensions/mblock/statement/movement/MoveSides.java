package de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.MBlockNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.Opcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.mblock.RobotMoveStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.MBlockVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class MoveSides extends AbstractNode implements RobotMoveStmt {

    private final NumExpr leftPower;
    private final NumExpr rightPower;
    private final BlockMetadata metadata;

    public MoveSides(NumExpr leftPower, NumExpr rightPower, BlockMetadata metadata) {
        super(leftPower, rightPower, metadata);
        this.leftPower = leftPower;
        this.rightPower = rightPower;
        this.metadata = metadata;
    }

    public NumExpr getLeftPower() {
        return leftPower;
    }

    public NumExpr getRightPower() {
        return rightPower;
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
        return RobotMoveStmtOpcode.move_with_motors;
    }
}
