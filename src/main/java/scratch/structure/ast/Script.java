package scratch.structure.ast;

public class Script {

    private BasicBlock block;
    private Subscript subscript;


    public Script(BasicBlock block) {
        this.block = block;
    }

    public Script(HatBlock block, Subscript subscript) {
        this.block = block;
        this.subscript = subscript;
    }


}
