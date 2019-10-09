package scratch.structure.ast.transformers;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.structure.ast.Ast;
import scratch.structure.ast.BasicBlock;

import java.util.Set;

public abstract class Transformer {

    /**
     * Constants used for serialization and deserialization for Scratch 3.
     * Names and values are the same as in the Scratch 3 source code.
     */
    int INPUT_SAME_BLOCK_SHADOW = 1; // unobscured shadow
    int INPUT_BLOCK_NO_SHADOW = 2; // no shadow
    int INPUT_DIFF_BLOCK_SHADOW = 3; // obscured shadow
    int MATH_NUM_PRIMITIVE = 4; // number
    int POSITIVE_NUM_PRIMITIVE = 5; // positive number
    int WHOLE_NUM_PRIMITIVE = 6; // positive integer
    int INTEGER_NUM_PRIMITIVE = 7; // integer
    int ANGLE_NUM_PRIMITIVE = 8; // angle
    int COLOR_PICKER_PRIMITIVE = 9; // colour
    int TEXT_PRIMITIVE = 10; // string
    int BROADCAST_PRIMITIVE = 11; // broadcast
    int VAR_PRIMITIVE = 12; // variable
    int LIST_PRIMITIVE = 13; // list

    protected String opcode;
    protected boolean topLevel;
    protected boolean shadow; //Returns the opcode(s)/id(s) this transformer works for.

    abstract Set<String> getIdentifiers();

    abstract BasicBlock transform(JsonNode node, Ast ast);

    protected void extractStandardValues(JsonNode node) {
        opcode = node.get("opcode").toString().replaceAll("^\"|\"$", "");
        topLevel = node.get("topLevel").asBoolean();
        shadow = node.get("shadow").asBoolean();
    }
}
