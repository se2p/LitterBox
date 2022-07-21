package de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.MBlockNode;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.LEDPosition;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.Opcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.mBlock.LEDStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.MBlockVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class RGBValuesPosition extends AbstractNode implements LEDStmt, PositionStmt {

    private final LEDPosition position;
    private final NumExpr red;
    private final NumExpr green;
    private final NumExpr blue;
    private final BlockMetadata metadata;

    public RGBValuesPosition(LEDPosition position, NumExpr red, NumExpr green, NumExpr blue, BlockMetadata metadata) {
        super(position, red, green, blue, metadata);
        this.position = position;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.metadata = metadata;
    }

    public LEDPosition getPosition() {
        return position;
    }

    public NumExpr getRed() {
        return red;
    }

    public NumExpr getGreen() {
        return green;
    }

    public NumExpr getBlue() {
        return blue;
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
        return LEDStmtOpcode.show_led_rgb;
    }
}
