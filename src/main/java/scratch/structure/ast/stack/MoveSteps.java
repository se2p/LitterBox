package scratch.structure.ast.stack;

import scratch.structure.ast.Extendable;
import scratch.structure.ast.Stackable;

public class MoveSteps extends StackBlock {

    public MoveSteps(String opcode,
                     Stackable next,
                     Extendable parent,
                     Object[] inputs,
                     Object[] fields,
                     boolean shadow,
                     boolean topLevel) {

        super(opcode, next, parent, inputs, fields, shadow, topLevel);
        if (!opcode.equals("motion_movesteps")) {
            //If this is always clear, should we even expect the opcode?
            throw new IllegalArgumentException("A move step block always has the OpCode 'motion_movesteps'");
        }
    }

    public MoveSteps(String opcode,
                     Stackable next,
                     Extendable parent,
                     Object[] inputs,
                     Object[] fields,
                     boolean shadow,
                     boolean topLevel,
                     int x,
                     int y) {
        super(opcode, next, parent, inputs, fields, shadow, topLevel, x, y);
        if (!opcode.equals("motion_movesteps")) {
            //If this is always clear, should we even expect the opcode?
            throw new IllegalArgumentException("A move step block always has the OpCode 'motion_movesteps'");
        }
    }
}
