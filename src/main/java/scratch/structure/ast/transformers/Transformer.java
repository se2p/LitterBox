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
    protected static final int INPUT_SAME_BLOCK_SHADOW = 1; // unobscured shadow
    protected static final int INPUT_BLOCK_NO_SHADOW = 2; // no shadow
    protected static final int INPUT_DIFF_BLOCK_SHADOW = 3; // obscured shadow
    protected static final int MATH_NUM_PRIMITIVE = 4; // number
    protected static final int POSITIVE_NUM_PRIMITIVE = 5; // positive number
    protected static final int WHOLE_NUM_PRIMITIVE = 6; // positive integer
    protected static final int INTEGER_NUM_PRIMITIVE = 7; // integer
    protected static final int ANGLE_NUM_PRIMITIVE = 8; // angle
    protected static final int COLOR_PICKER_PRIMITIVE = 9; // colour
    protected static final int TEXT_PRIMITIVE = 10; // string
    protected static final int BROADCAST_PRIMITIVE = 11; // broadcast
    protected static final int VAR_PRIMITIVE = 12; // variable
    protected static final int LIST_PRIMITIVE = 13; // list

    /**
     * The terms "input array" and "(input) data array" refer to specific parts
     * in the JSON file where inputs are stored.
     *
     * In the example
     * "DEGREES": [1,[4,"15"]]
     *
     * [1,[4,"15"]] is the input array
     * holding the input shadow indicator and the data array
     *
     * and [4,"15"] is the data array holding the input type and the input value.
     */

    /**
     * The position of the input shadow indicator in the input array.
     */
    protected static final int POS_INPUT_SHADOW = 0;

    /**
     * The position of the data array holding input type and input value in the input array.
     */
    protected static final int POS_DATA_ARRAY = 1;

    /**
     * The position of the input type in the input data array.
     */
    protected static final int POS_INPUT_TYPE = 0;

    /**
     * The position of the input value in the input data array.
     */
    protected static final int POS_INPUT_VALUE = 1;

    /**
     * The position of the inputVariableID in the data array.
     */
    protected static final int POS_VAR_ID = 2;

    protected String opcode;
    protected boolean topLevel;
    protected boolean shadow;

    abstract Set<String> getIdentifiers(); //Returns the opcode(s)/id(s) this transformer works for.

    abstract BasicBlock transform(JsonNode node, Ast ast);

    protected void extractStandardValues(JsonNode node) {
        opcode = node.get("opcode").toString().replaceAll("^\"|\"$", "");
        topLevel = node.get("topLevel").asBoolean();
        shadow = node.get("shadow").asBoolean();
    }
}
