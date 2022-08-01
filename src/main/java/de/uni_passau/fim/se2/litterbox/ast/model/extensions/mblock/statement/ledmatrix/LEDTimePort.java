package de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.MBlockNode;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.MCorePort;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.Opcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.mBlock.LEDMatrixStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.MBlockVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class LEDTimePort extends AbstractNode implements LEDMatrixStmt, PortStmt {

    private final MCorePort port;
    private final NumExpr hour;
    private final NumExpr minute;
    private final BlockMetadata metadata;

    public LEDTimePort(MCorePort port, NumExpr hour, NumExpr minute, BlockMetadata metadata) {
        super(port, hour, minute, metadata);
        this.port = port;
        this.hour = hour;
        this.minute = minute;
        this.metadata = metadata;
    }

    public MCorePort getPort() {
        return port;
    }

    public NumExpr getHour() {
        return hour;
    }

    public NumExpr getMinute() {
        return minute;
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
        return LEDMatrixStmtOpcode.show_time;
    }
}
