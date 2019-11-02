package scratch.newast.model.expression;

import scratch.newast.model.ScratchEntity;

public class AttributeOf implements StringExpr {
    private StringExpr attribute;
    private ScratchEntity entity;

    public AttributeOf(StringExpr attribute, ScratchEntity entity) {
        this.attribute = attribute;
        this.entity = entity;
    }

    public StringExpr getAttribute() {
        return attribute;
    }

    public void setAttribute(StringExpr attribute) {
        this.attribute = attribute;
    }

    public ScratchEntity getEntity() {
        return entity;
    }

    public void setEntity(ScratchEntity entity) {
        this.entity = entity;
    }
}