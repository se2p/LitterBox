package de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.MBlockNode;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.LEDMatrix;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.Opcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.mBlock.LEDMatrixStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.MBlockVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class FacePosition extends AbstractNode implements LEDMatrixStmt, FacePanelStmt {

    private final LEDMatrix ledMatrix;
    private final NumExpr xAxis;
    private final NumExpr yAxis;
    private final BlockMetadata metadata;

    public FacePosition(LEDMatrix ledMatrix, NumExpr xAxis, NumExpr yAxis, BlockMetadata metadata) {
        super(ledMatrix, xAxis, yAxis, metadata);
        this.ledMatrix = ledMatrix;
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.metadata = metadata;
    }

    public LEDMatrix getLedMatrix() {
        return ledMatrix;
    }

    public NumExpr getxAxis() {
        return xAxis;
    }

    public NumExpr getyAxis() {
        return yAxis;
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
        return LEDMatrixStmtOpcode.show_led_matrix_face_position;
    }
}
