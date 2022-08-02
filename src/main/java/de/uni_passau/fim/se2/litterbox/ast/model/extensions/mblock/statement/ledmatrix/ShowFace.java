package de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.MBlockNode;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.LEDMatrix;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.Opcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.mblock.LEDMatrixStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.MBlockVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class ShowFace extends AbstractNode implements LEDMatrixStmt, FacePanelStmt {

    private final LEDMatrix ledMatrix;
    private final BlockMetadata metadata;

    public ShowFace(LEDMatrix ledMatrix, BlockMetadata metadata) {
        super(ledMatrix, metadata);
        this.ledMatrix = ledMatrix;
        this.metadata = metadata;
    }

    public LEDMatrix getLedMatrix() {
        return ledMatrix;
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
        return LEDMatrixStmtOpcode.show_led_matrix_face;
    }
}
