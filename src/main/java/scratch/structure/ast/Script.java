package scratch.structure.ast;

import scratch.structure.ast.hat.HatBlock;

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
