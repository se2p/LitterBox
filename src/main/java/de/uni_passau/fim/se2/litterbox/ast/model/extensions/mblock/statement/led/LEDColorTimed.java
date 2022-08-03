package de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.MBlockNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.TimedStmt;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.Opcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.mblock.LEDStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.MBlockVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class LEDColorTimed extends AbstractNode implements LEDStmt, TimedStmt, ColorStmt {

    private final StringExpr colorString;
    private final NumExpr time;
    private final BlockMetadata metadata;

    public LEDColorTimed(StringExpr colorString, NumExpr time, BlockMetadata metadata) {
        super(colorString, time, metadata);
        this.colorString = colorString;
        this.time = time;
        this.metadata = metadata;
    }

    public StringExpr getColorString() {
        return colorString;
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
        return LEDStmtOpcode.show_led_with_time;
    }
}
