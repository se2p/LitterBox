package scratch.structure.ast;

import scratch.structure.ast.visitor.BlockVisitor;

public abstract class ScratchBlock {

    private String opcode;
    protected boolean shadow;
    protected boolean topLevel;
    protected int x;
    protected int y;

    public ScratchBlock(String opcode) {
        this.opcode = opcode;
    }

    public abstract void accept(BlockVisitor visitor);

    public String getOpcode() {
        return opcode;
    }

    public void setOpcode(String opcode) {
        this.opcode = opcode;
    }

    public boolean isShadow() {
        return shadow;
    }

    public void setShadow(boolean shadow) {
        this.shadow = shadow;
    }

    public boolean isTopLevel() {
        return topLevel;
    }

    public void setTopLevel(boolean topLevel) {
        this.topLevel = topLevel;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
