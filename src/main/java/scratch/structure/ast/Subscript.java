package scratch.structure.ast;

import java.util.Arrays;
import java.util.List;

public class Subscript {

    CapBlock capBlock;
    List<ScriptBodyBlock> bodyBlocks;

    public Subscript(ScriptBodyBlock... scriptBodyBlocks) {
        this.bodyBlocks = Arrays.asList(scriptBodyBlocks);
    }

    public Subscript(List<ScriptBodyBlock> scriptBodyBlocks) {
        this.bodyBlocks = scriptBodyBlocks;
    }

    public Subscript(List<ScriptBodyBlock> scriptBodyBlocks, CapBlock capBlock) {
        this.bodyBlocks = scriptBodyBlocks;
        this.capBlock = capBlock;
    }
}
