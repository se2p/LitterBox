package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class SoundMetadata extends ResourceMetadata {

    private int rate;
    private int sampleCount;

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
