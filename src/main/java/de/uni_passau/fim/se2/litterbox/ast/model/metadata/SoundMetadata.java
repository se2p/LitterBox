package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class SoundMetadata extends ResourceMetadata {

    private int rate;
    private int sampleCount;

    public SoundMetadata(String assetId, String name, String md5ext, String dataFormat, int rate, int sampleCount) {
        super(assetId, name, md5ext, dataFormat);
        this.rate = rate;
        this.sampleCount = sampleCount;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public void setSampleCount(int sampleCount) {
        this.sampleCount = sampleCount;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
