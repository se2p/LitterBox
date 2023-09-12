package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util;

public class MaskingStrategy {
    private final MaskingType maskingType;
    private final String blockId;

    public MaskingStrategy(MaskingType maskingType, String blockId) {
        this.maskingType = maskingType;
        this.blockId = blockId;
    }

    public MaskingType getMaskingType() {
        return maskingType;
    }

    public String getBlockId() {
        return blockId;
    }
}
