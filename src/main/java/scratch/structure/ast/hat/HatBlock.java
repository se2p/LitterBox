package scratch.structure.ast.hat;

import scratch.structure.ast.BasicBlock;
import scratch.structure.ast.Extendable;
import scratch.structure.ast.Stackable;

public abstract class HatBlock implements Extendable, BasicBlock {

    private String opcode;
    private Stackable next;
    //    private Extendable parent;  //Hat Blocks have the parent field in json but do not ever have parents
    //    private Object[] inputs; //Todo: Make this more specific, once we have proper types for inputs
    //    private Object[] fields; //Todo: Make this more specific, once we have proper types for fields
    private boolean shadow;
    private boolean topLevel; //This most likely will always be true
    private int x;
    private int y;

    public HatBlock(String opcode, Stackable next, boolean shadow, boolean topLevel, int x, int y) {
        this.opcode = opcode;
        this.next = next;
        this.shadow = shadow;
        this.topLevel = topLevel;
        this.x = x;
        this.y = y;
    }

    public String getOpcode() {
        return opcode;
    }

    public void setOpcode(String opcode) {
        this.opcode = opcode;
    }

    @Override
    public Stackable getNext() {
        return next;
    }

    @Override
    public void setNext(Stackable next) {
        this.next = next;
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
