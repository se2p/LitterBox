package de.uni_passau.fim.se2.litterbox.ast.model.metadata.block;

import de.uni_passau.fim.se2.litterbox.ast.model.metadata.input.DataInputMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class DataBlockMetadata extends DataInputMetadata implements BlockMetadata {
    double x;
    double y;

    public DataBlockMetadata(String inputName, int dataType, String dataName, String dataReference, double x,
                             double y) {
        super(inputName, dataType, dataName, dataReference);
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
